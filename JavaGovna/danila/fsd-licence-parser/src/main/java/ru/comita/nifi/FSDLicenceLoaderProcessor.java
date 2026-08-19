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
import ru.comita.nifi.service.FSDLicenceExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({ "EXCEL", "FSD", "Licence" })
@CapabilityDescription("FSD Licence Excel loading")
public class FSDLicenceLoaderProcessor extends FSDLoaderProcessor<FSDLicenceExcelLoaderService> {
    public static final String LICENСE_SCHEMA_JSON = "licence-schema.json";

    private static final String defaultLicenceSchema;

    static {
        try (InputStream defaultLicenceSchemaIn = FSDLicenceLoaderProcessor.class
                .getClassLoader().getResourceAsStream(LICENСE_SCHEMA_JSON);) {
            if (defaultLicenceSchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultLicenceSchema = new String(defaultLicenceSchemaIn.readAllBytes(),
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

    public static final PropertyDescriptor LICENСE_SCHEMA = new PropertyDescriptor.Builder()
            .name("Licence Schema")
            .displayName("Licence Schema")
            .required(true)
            .description("Схема для парсинга НСИ Лицензия")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDLicenceLoaderProcessor.defaultLicenceSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, LICENСE_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.LICENCE, context.getProperty("Licence Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDLicenceExcelLoaderService(sheetNum, formCell, connection);
    }

}