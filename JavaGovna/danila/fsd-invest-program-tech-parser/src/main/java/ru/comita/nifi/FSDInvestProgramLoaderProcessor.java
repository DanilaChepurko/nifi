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
import ru.comita.nifi.service.FSDInvestProgramExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({"EXCEL", "FSD", "InvestProgram"})
@CapabilityDescription("FSD InvestProgram Excel loading")
public class FSDInvestProgramLoaderProcessor extends FSDLoaderProcessor<FSDInvestProgramExcelLoaderService> {
    public static final String INVEST_PROGRAM_SCHEMA_JSON = "invest-program-schema.json";
    private static final String defaultInvestProgramSchema;

    static {
        try (InputStream defaultInvestProgramSchemaIn = FSDInvestProgramLoaderProcessor.class
                     .getClassLoader().getResourceAsStream(INVEST_PROGRAM_SCHEMA_JSON)) {
            if (defaultInvestProgramSchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultInvestProgramSchema = new String(defaultInvestProgramSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
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

    public static final PropertyDescriptor INVEST_PROGRAM_SCHEMA = new PropertyDescriptor.Builder()
            .name("InvestProgram Schema")
            .displayName("InvestProgram Schema")
            .required(true)
            .description("Схема для парсинга мероприятий")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDInvestProgramLoaderProcessor.defaultInvestProgramSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, INVEST_PROGRAM_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.HEADER, context.getProperty("InvestProgram Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDInvestProgramExcelLoaderService(sheetNum, formCell, connection);
    }

}