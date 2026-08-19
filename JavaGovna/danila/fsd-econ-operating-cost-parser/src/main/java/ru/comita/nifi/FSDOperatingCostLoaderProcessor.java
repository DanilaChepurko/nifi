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
import ru.comita.nifi.service.FSDOperatingCostExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({"EXCEL", "FSD", "FSD10"})
@CapabilityDescription("FSD10 Excel loading")
public class FSDOperatingCostLoaderProcessor extends FSDLoaderProcessor<FSDOperatingCostExcelLoaderService> {
    public static final String HEADER_SCHEMA_JSON = "header-schema.json";
    public static final String OPF_SCHEMA_JSON = "opf-schema.json";
    public static final String PARAM_SCHEMA_JSON = "param-schema.json";

    private static final String defaultHeaderSchema;
    private static final String defaultOpfSchema;

    private static final String defaultParamSchema;

    static {
        try (InputStream defaultHeaderSchemaIn = FSDOperatingCostLoaderProcessor.class
                .getClassLoader().getResourceAsStream(HEADER_SCHEMA_JSON);
             InputStream defaultParamSchemaIn = FSDOperatingCostLoaderProcessor.class
                     .getClassLoader().getResourceAsStream(PARAM_SCHEMA_JSON);
             InputStream defaultOpfSchemaIn=FSDOperatingCostLoaderProcessor.class.getClassLoader().getResourceAsStream(OPF_SCHEMA_JSON)) {
            if (defaultHeaderSchemaIn == null || defaultParamSchemaIn == null||defaultOpfSchemaIn==null) {
                throw new ResourceNotFoundException();
            }
            defaultHeaderSchema = new String(defaultHeaderSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            defaultParamSchema = new String(defaultParamSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            defaultOpfSchema=new String(defaultOpfSchemaIn.readAllBytes(),StandardCharsets.UTF_8);
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
            .defaultValue(FSDOperatingCostLoaderProcessor.defaultHeaderSchema)
            .build();

    public static final PropertyDescriptor PARAM_SCHEMA = new PropertyDescriptor.Builder()
            .name("Param Schema")
            .displayName("Param Schema")
            .required(true)
            .description("Схема для парсинга регистра")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDOperatingCostLoaderProcessor.defaultParamSchema)
            .build();
    public static final PropertyDescriptor OPF_SCHEMA = new PropertyDescriptor.Builder()
            .name("Opf Schema")
            .displayName("Opf Schema")
            .required(true)
            .description("Схема для парсинга ОПФ")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDOperatingCostLoaderProcessor.defaultOpfSchema)
            .build();


    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, HEADER_SCHEMA, PARAM_SCHEMA,OPF_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.HEADER, context.getProperty("Header Schema").getValue());
        schemaMap.put(SchemaName.HORIZON_PARAM, context.getProperty("Param Schema").getValue());
        schemaMap.put(SchemaName.OPF,context.getProperty("Opf Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDOperatingCostExcelLoaderService(sheetNum, formCell, connection);
    }

}