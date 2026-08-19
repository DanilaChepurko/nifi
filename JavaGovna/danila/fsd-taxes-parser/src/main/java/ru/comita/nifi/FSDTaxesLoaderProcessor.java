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
import ru.comita.nifi.service.FSDTaxesExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({ "EXCEL", "FSD", "Taxes" })
@CapabilityDescription("FSD Taxes Excel loading")
public class FSDTaxesLoaderProcessor extends FSDLoaderProcessor<FSDTaxesExcelLoaderService> {
    public static final String TAXES_SCHEMA_JSON = "taxes-schema.json";
    public static final String HEADER_SCHEMA_JSON = "econ-header-schema.json";

    private static final String defaultTaxesSchema;
    private static final String defaultHeaderSchema;

    static {
        try (InputStream defaultTaxesSchemaIn = FSDTaxesLoaderProcessor.class
                .getClassLoader().getResourceAsStream(TAXES_SCHEMA_JSON);
                InputStream defaultHeaderSchemaIn = FSDTaxesLoaderProcessor.class
                        .getClassLoader().getResourceAsStream(HEADER_SCHEMA_JSON)) {
            if (defaultTaxesSchemaIn == null || defaultHeaderSchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultTaxesSchema = new String(defaultTaxesSchemaIn.readAllBytes(),
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
            .defaultValue("2")
            .build();

    public static final PropertyDescriptor TAXES_SCHEMA = new PropertyDescriptor.Builder()
            .name("Taxes Schema")
            .displayName("Taxes Schema")
            .required(true)
            .description("Схема для парсинга налогов")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDTaxesLoaderProcessor.defaultTaxesSchema)
            .build();

    public static final PropertyDescriptor HEADER_SCHEMA = new PropertyDescriptor.Builder()
            .name("Header Schema")
            .displayName("Header Schema")
            .required(true)
            .description("Схема для парсинга шапки налогов")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDTaxesLoaderProcessor.defaultHeaderSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, TAXES_SCHEMA, HEADER_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.TAXES, context.getProperty("Taxes Schema").getValue());
        schemaMap.put(SchemaName.HEADER, context.getProperty("Header Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDTaxesExcelLoaderService(sheetNum, formCell, connection);
    }

}