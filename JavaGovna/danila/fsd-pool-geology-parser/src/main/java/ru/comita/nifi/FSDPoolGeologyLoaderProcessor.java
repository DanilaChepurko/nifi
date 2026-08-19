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
import ru.comita.nifi.service.FSDPoolGeologyExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({ "EXCEL", "FSD", "Pool geology" })
@CapabilityDescription("FSD PoolGeology Excel loading")
public class FSDPoolGeologyLoaderProcessor extends FSDLoaderProcessor<FSDPoolGeologyExcelLoaderService> {
    public static final String POOL_GEOLOGY_SCHEMA_JSON = "pool-geology-schema.json";

    private static final String defaultPoolGeologySchema;

    static {
        try (InputStream defaultPoolGeologySchemaIn = FSDPoolGeologyLoaderProcessor.class
                .getClassLoader().getResourceAsStream(POOL_GEOLOGY_SCHEMA_JSON);) {
            if (defaultPoolGeologySchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultPoolGeologySchema = new String(defaultPoolGeologySchemaIn.readAllBytes(),
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

    public static final PropertyDescriptor POOL_GEOLOGY_SCHEMA = new PropertyDescriptor.Builder()
            .name("Pool Geology Schema")
            .displayName("Pool Geology Schema")
            .required(true)
            .description("Схема для парсинга НСИ Геология объекта")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDPoolGeologyLoaderProcessor.defaultPoolGeologySchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, POOL_GEOLOGY_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.POOL_GEOLOGY, context.getProperty("Pool Geology Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDPoolGeologyExcelLoaderService(sheetNum, formCell, connection);
    }

}