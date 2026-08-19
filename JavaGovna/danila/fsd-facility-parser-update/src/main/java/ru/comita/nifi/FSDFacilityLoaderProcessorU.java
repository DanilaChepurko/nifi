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
import ru.comita.nifi.service.FSDFacilityExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({"EXCEL", "FSD", "Facility"})
@CapabilityDescription("FSD Facility Excel loading")
public class FSDFacilityLoaderProcessorU extends FSDLoaderProcessor<FSDFacilityExcelLoaderService> {
    public static final String FACILITY_SCHEMA_JSON = "facility-schema.json";
    private static final String defaultFacilitySchema;

    static {
        try (InputStream defaultFacilitySchemaIn = FSDFacilityLoaderProcessorU.class
                .getClassLoader().getResourceAsStream(FACILITY_SCHEMA_JSON)) {
            if (defaultFacilitySchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultFacilitySchema = new String(defaultFacilitySchemaIn.readAllBytes(),
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

    public static final PropertyDescriptor FACILITY_SCHEMA = new PropertyDescriptor.Builder()
            .name("Facility Schema")
            .displayName("Facility Schema")
            .required(true)
            .description("Схема для парсинга площадок")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDFacilityLoaderProcessorU.defaultFacilitySchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, FACILITY_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.FACILITY, context.getProperty("Facility Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDFacilityExcelLoaderService(sheetNum, formCell, connection);
    }

}