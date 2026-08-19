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
import ru.comita.nifi.service.FSDProjectStepExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({"EXCEL", "FSD", "Project Step"})
@CapabilityDescription("FSD Project Step Excel loading")
public class FSDProjectStepLoaderProcessorU extends FSDLoaderProcessor<FSDProjectStepExcelLoaderService> {
    public static final String PROJECT_STEP_SCHEMA_JSON = "project-step-schema.json";
    private static final String defaultProjectStepSchema;

    static {
        try (             InputStream defaultProjectStepSchemaIn = FSDProjectStepLoaderProcessorU.class
                     .getClassLoader().getResourceAsStream(PROJECT_STEP_SCHEMA_JSON)) {
            if (defaultProjectStepSchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultProjectStepSchema = new String(defaultProjectStepSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
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

    public static final PropertyDescriptor PROJECT_STEP_SCHEMA = new PropertyDescriptor.Builder()
            .name("Project Step Schema")
            .displayName("Project Step Schema")
            .required(true)
            .description("Схема для парсинга мероприятий")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDProjectStepLoaderProcessorU.defaultProjectStepSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, PROJECT_STEP_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.PROJECT_STEP, context.getProperty("Project Step Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDProjectStepExcelLoaderService(sheetNum, formCell, connection);
    }

}