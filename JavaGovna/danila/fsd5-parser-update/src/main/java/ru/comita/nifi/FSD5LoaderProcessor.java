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
import ru.comita.nifi.service.FSD5ExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({"EXCEL", "FSD", "FSD5"})
@CapabilityDescription("FSD5 Excel loading")
public class FSD5LoaderProcessor extends FSDLoaderProcessor<FSD5ExcelLoaderService> {
    public static final String HEADER_SCHEMA_JSON = "header-schema.json";
    public static final String VOL_SUMMARY_SCHEMA_JSON = "vol-summary-schema.json";
    private static final String defaultHeaderSchema;
    private static final String defaultVolSummarySchema;

    static {
        try (InputStream defaultHeaderSchemaIn = FSD5LoaderProcessor.class
                .getClassLoader().getResourceAsStream(HEADER_SCHEMA_JSON);
             InputStream defaultVolSummarySchemaIn = FSD5LoaderProcessor.class
                     .getClassLoader().getResourceAsStream(VOL_SUMMARY_SCHEMA_JSON)) {
            if (defaultHeaderSchemaIn == null || defaultVolSummarySchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultHeaderSchema = new String(defaultHeaderSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            defaultVolSummarySchema = new String(defaultVolSummarySchemaIn.readAllBytes(), StandardCharsets.UTF_8);
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
            .defaultValue(FSD5LoaderProcessor.defaultHeaderSchema)
            .build();

    public static final PropertyDescriptor VOL_SUMMARY_SCHEMA = new PropertyDescriptor.Builder()
            .name("Vol Summary Schema")
            .displayName("Vol Summary Schema")
            .required(true)
            .description("Схема для парсинга регистра")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSD5LoaderProcessor.defaultVolSummarySchema)
            .build();


    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, HEADER_SCHEMA, VOL_SUMMARY_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.HEADER, context.getProperty("Header Schema").getValue());
        schemaMap.put(SchemaName.VOL_SUMMARY, context.getProperty("Vol Summary Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSD5ExcelLoaderService(sheetNum, formCell, connection);
    }

}