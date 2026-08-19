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
import ru.comita.nifi.service.FSD2ExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({"EXCEL", "FSD", "FSD2"})
@CapabilityDescription("FSD2 Excel loading")
public class FSD2uLoaderProcessor extends FSDLoaderProcessor<FSD2ExcelLoaderService> {
    public static final String HEADER_SCHEMA_JSON = "header-schema.json";
    public static final String VOL_SUMMARY_SCHEMA_JSON = "vol-summary-schema.json";
    public static final String OPTIONS_SCHEMA_JSON = "options-schema.json";
    private static final String defaultHeaderSchema;
    private static final String defaultVolSummarySchema;
    private static final String defaultOptionsSchema;

    static {
        try (InputStream defaultHeaderSchemaIn = FSD2uLoaderProcessor.class
                .getClassLoader().getResourceAsStream(HEADER_SCHEMA_JSON);
             InputStream defaultVolSummarySchemaIn = FSD2uLoaderProcessor.class
                     .getClassLoader().getResourceAsStream(VOL_SUMMARY_SCHEMA_JSON);
             InputStream defaultOptionsSchemaIn = FSD2uLoaderProcessor.class
                     .getClassLoader().getResourceAsStream(OPTIONS_SCHEMA_JSON)) {
            if (defaultHeaderSchemaIn == null || defaultVolSummarySchemaIn == null || defaultOptionsSchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultHeaderSchema = new String(defaultHeaderSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            defaultVolSummarySchema = new String(defaultVolSummarySchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            defaultOptionsSchema = new String(defaultOptionsSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
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
            .defaultValue("2")
            .build();

    public static final PropertyDescriptor HEADER_SCHEMA = new PropertyDescriptor.Builder()
            .name("Header Schema")
            .displayName("Header Schema")
            .required(true)
            .description("Схема для парсинга заголовочной части")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSD2uLoaderProcessor.defaultHeaderSchema)
            .build();

    public static final PropertyDescriptor VOL_SUMMARY_SCHEMA = new PropertyDescriptor.Builder()
            .name("Vol Summary Schema")
            .displayName("Vol Summary Schema")
            .required(true)
            .description("Схема для парсинга регистра")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSD2uLoaderProcessor.defaultVolSummarySchema)
            .build();

    public static final PropertyDescriptor OPTIONS_SCHEMA = new PropertyDescriptor.Builder()
            .name("Options Schema")
            .displayName("Options Schema")
            .required(true)
            .description("Схема для парсинга вариантов")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSD2uLoaderProcessor.defaultOptionsSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, HEADER_SCHEMA, VOL_SUMMARY_SCHEMA, OPTIONS_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.HEADER, context.getProperty("Header Schema").getValue());
        schemaMap.put(SchemaName.VOL_SUMMARY, context.getProperty("Vol Summary Schema").getValue());
        schemaMap.put(SchemaName.OPTIONS, context.getProperty("Options Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSD2ExcelLoaderService(sheetNum, formCell, connection);
    }
}