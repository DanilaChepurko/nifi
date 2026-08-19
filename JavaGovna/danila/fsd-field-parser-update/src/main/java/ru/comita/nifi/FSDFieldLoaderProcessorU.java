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
import ru.comita.nifi.service.FSDFieldExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({"EXCEL", "FSD", "Field"})
@CapabilityDescription("FSD Field Excel loading")
public class FSDFieldLoaderProcessorU extends FSDLoaderProcessor<FSDFieldExcelLoaderService> {
    public static final String FIELD_SCHEMA_JSON = "field-schema.json";
    private static final String defaultFieldSchema;

    static {
        try (InputStream defaultFieldSchemaIn = FSDFieldLoaderProcessorU.class
                .getClassLoader().getResourceAsStream(FIELD_SCHEMA_JSON)) {
            if (defaultFieldSchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultFieldSchema = new String(defaultFieldSchemaIn.readAllBytes(),
                    StandardCharsets.UTF_8);
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

    public static final PropertyDescriptor FIELD_SCHEMA = new PropertyDescriptor.Builder()
            .name("Field Schema")
            .displayName("Field Schema")
            .required(true)
            .description("Схема для парсинга месторождений")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDFieldLoaderProcessorU.defaultFieldSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(FIELD_SCHEMA, SHEET_NUMBER);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.FIELD, context.getProperty("Field Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDFieldExcelLoaderService(sheetNum, formCell, connection);
    }

}