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
import ru.comita.nifi.service.FSDFluidTypesExcelLoaderService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideEffectFree
@Tags({ "EXCEL", "FSD", "Fluid Types" })
@CapabilityDescription("FSD FluidTypes Excel loading")
public class FSDFluidTypesLoaderProcessor extends FSDLoaderProcessor<FSDFluidTypesExcelLoaderService> {
    public static final String FLUID_TYPES_SCHEMA_JSON = "fluid-types-schema.json";

    private static final String defaultFluidTypesSchema;

    static {
        try (InputStream defaultFluidTypesSchemaIn = FSDFluidTypesLoaderProcessor.class
                .getClassLoader().getResourceAsStream(FLUID_TYPES_SCHEMA_JSON);) {
            if (defaultFluidTypesSchemaIn == null) {
                throw new ResourceNotFoundException();
            }
            defaultFluidTypesSchema = new String(defaultFluidTypesSchemaIn.readAllBytes(),
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

    public static final PropertyDescriptor FLUID_TYPES_SCHEMA = new PropertyDescriptor.Builder()
            .name("Fluid types Schema")
            .displayName("Fluid types Schema")
            .required(true)
            .description("Схема для парсинга НСИ Виды Флюида")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue(FSDFluidTypesLoaderProcessor.defaultFluidTypesSchema)
            .build();

    @Override
    public List<PropertyDescriptor> schemaProperties() {
        return List.of(SHEET_NUMBER, FLUID_TYPES_SCHEMA);
    }

    @Override
    public Map<SchemaName, String> getSchemaMap(ProcessContext context) {
        Map<SchemaName, String> schemaMap = new HashMap<>();
        schemaMap.put(SchemaName.FLUID_TYPES, context.getProperty("Fluid types Schema").getValue());
        return schemaMap;
    }

    @Override
    public void initFsdExcelLoaderService(String sheetNum, String formCell, Connection connection) {
        this.fsdExcelLoaderService = new FSDFluidTypesExcelLoaderService(sheetNum, formCell, connection);
    }

}