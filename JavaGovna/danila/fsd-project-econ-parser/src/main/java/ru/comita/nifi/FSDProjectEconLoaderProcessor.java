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
import ru.comita.nifi.service.FSDProjectEconExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({ "EXCEL", "FSD", "ProjectEcon " })
@CapabilityDescription("FSD ProjectEcon  Excel loading")
public class FSDProjectEconLoaderProcessor extends FSDLoaderProcessor<FSDProjectEconExcelLoaderService> {
    public static final String PROJECT_ECON_SCHEMA_JSON = "project-step-econ-schema.json";
    public static final String HEADER_SCHEMA_JSON = "econ-header-schema.json";

    private static final String defaultProjectEconSchema;
    private static final String defaultHeaderSchema;

    static {
        try (InputStream defaultProjectEconSchemaIn = FSDProjectEconLoaderProcessor.class
                .getClassLoader().getResourceAsStream(PROJECT_ECON_SCHEMA_JSON);
                InputStream defaultHeaderSchemaIn = FSDProjectEconLoaderProcessor.class
                        .getClassLoader().getResourceAsStream(HEADER_SCHEMA_JSON)) {
            if (defaultProjectEconSchemaIn == null || defaultHeaderSchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultProjectEconSchema = new String(defaultProjectEconSchemaIn.readAllBytes(),
                    StandardCharsets.UTF_8);
            defaultHeaderSchema = new String(defaultHeaderSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
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

    public static final PropertyDescriptor PROJECT_ECON_SCHEMA = new PropertyDescriptor.Builder()
            .name("ProjectStepEcon Schema")
            .displayName("ProjectStepEcon Schema")
            .required(true)
            .description("Схема для парсинга инвестпрограммы")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDProjectEconLoaderProcessor.defaultProjectEconSchema)
            .build();

    public static final PropertyDescriptor HEADER_SCHEMA = new PropertyDescriptor.Builder()
            .name("Header Schema")
            .displayName("Header Schema")
            .required(true)
            .description("Схема для парсинга шапки")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDProjectEconLoaderProcessor.defaultHeaderSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, PROJECT_ECON_SCHEMA, HEADER_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.PROJECT_ECON, context.getProperty("ProjectStepEcon Schema").getValue());
        schemaMap.put(SchemaName.HEADER, context.getProperty("Header Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDProjectEconExcelLoaderService(sheetNum, formCell, connection);
    }

}