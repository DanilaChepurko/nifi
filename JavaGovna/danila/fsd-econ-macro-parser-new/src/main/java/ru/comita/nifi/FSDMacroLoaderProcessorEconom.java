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
import ru.comita.nifi.service.FSDMacroExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({"EXCEL", "FSD", "Macro"})
@CapabilityDescription("FSD Macro Excel loading")
public class FSDMacroLoaderProcessorEconom extends FSDLoaderProcessor<FSDMacroExcelLoaderService> {
    public static final String MACRO_SCHEMA_JSON = "macro-schema.json";
    public static final String HEADER_SCHEMA_JSON= "header-schema.json";
    private static final String defaultMacroSchema;
    private static final String defaultHeaderSchema;


    static {
        try (InputStream defaultHederSchemaIn = FSDMacroLoaderProcessorEconom.class
                .getClassLoader().getResourceAsStream(HEADER_SCHEMA_JSON);
            InputStream defaultMacroSchemaIn = FSDMacroLoaderProcessorEconom.class
                                .getClassLoader().getResourceAsStream(MACRO_SCHEMA_JSON)) {
            if (defaultMacroSchemaIn == null||defaultHederSchemaIn==null) {
                throw new ResourceNotFoundException();
            }
            defaultMacroSchema = new String(defaultMacroSchemaIn.readAllBytes(),
                    StandardCharsets.UTF_8);
            defaultHeaderSchema = new String(defaultHederSchemaIn.readAllBytes(),
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
            .defaultValue("2")
            .build();

    public static final PropertyDescriptor HEADER_SCHEMA = new PropertyDescriptor.Builder()
            .name("Header Schema")
            .displayName("Header Schema")
            .required(true)
            .description("Схема для парсинга заголовочной части")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDMacroLoaderProcessorEconom.defaultHeaderSchema)
            .build();

    public static final PropertyDescriptor MACRO_SCHEMA = new PropertyDescriptor.Builder()
            .name("Macro Schema")
            .displayName("Macro Schema")
            .required(true)
            .description("Схема для парсинга Макроуровня")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDMacroLoaderProcessorEconom.defaultMacroSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, MACRO_SCHEMA,HEADER_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.HEADER, context.getProperty("Header Schema").getValue());
        schemaMap.put(SchemaName.MACRO, context.getProperty("Macro Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDMacroExcelLoaderService(sheetNum, formCell, connection);
    }

}