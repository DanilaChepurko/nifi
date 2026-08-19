package ru.comita.nifi;

import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.util.StandardValidators;
import ru.comita.lib.FSDLoaderProcessor;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.ResourceNotFoundException;
import ru.comita.lib.exception.ResourceParseException;
import ru.comita.nifi.service.FSDProjectEventEquipmentExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({"EXCEL", "FSD", "Project equipment Event"})
@CapabilityDescription("FSD Project Event equipment Excel loading")
public class FSDProjectEventEquipmentLoaderProcessor extends FSDLoaderProcessor<FSDProjectEventEquipmentExcelLoaderService> {
    public static final String PROJECT_EVENT_EQUIPMENT_SCHEMA_JSON = "project-event-schema.json";
    public static final String HEADER_SCHEMA_JSON = "header-schema.json";
    private static final String defaultHeaderSchema;
    private static final String defaultProjectEventEquipmentSchema;

    static {
        try (InputStream defaultHeaderSchemaIn = FSDProjectEventEquipmentLoaderProcessor.class.getClassLoader().getResourceAsStream(HEADER_SCHEMA_JSON);
             InputStream defaultProjectEventEquipmentSchemaIn = FSDProjectEventEquipmentLoaderProcessor.class.getClassLoader().getResourceAsStream(PROJECT_EVENT_EQUIPMENT_SCHEMA_JSON)
        ) {
            if (defaultHeaderSchemaIn == null || defaultProjectEventEquipmentSchemaIn == null) {
                throw new ResourceNotFoundException();

            }
            defaultHeaderSchema = new String(defaultHeaderSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            defaultProjectEventEquipmentSchema = new String(defaultProjectEventEquipmentSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ResourceParseException(e);
        }
    }

    public static final PropertyDescriptor SHEET_NUMBER = new PropertyDescriptor.Builder()
            .name("Sheet number")
            .displayName("Sheet number")
            .required(true)
            .description("Номер первой страницы с данными")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue("1")
            .build();

    public static final PropertyDescriptor HEADER_SCHEMA = new PropertyDescriptor.Builder()
            .name("Header Schema")
            .displayName("Header Schema")
            .required(true)
            .description("Схема для парсинга заголовочной части")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDProjectEventEquipmentLoaderProcessor.defaultHeaderSchema)
            .build();

    public static final PropertyDescriptor PROJECT_EVENT_SCHEMA = new PropertyDescriptor.Builder()
            .name("Project Event Schema")
            .displayName("Project Event Schema")
            .required(true)
            .description("Схема для парсинга мероприятий")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDProjectEventEquipmentLoaderProcessor.defaultProjectEventEquipmentSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER,HEADER_SCHEMA, PROJECT_EVENT_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.HEADER, context.getProperty("Header Schema").getValue());
        schemaMap.put(SchemaName.PROJECT_EVENT, context.getProperty("Project Event Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDProjectEventEquipmentExcelLoaderService(sheetNum, formCell, connection);
    }

}