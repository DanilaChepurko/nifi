package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDFluidTypesExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDFluidTypesExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDFluidTypesExcelLoaderService fsdFluidTypesExcelLoaderService;
    private final FsdFluidTypesConnectionManager connectionManager = new FsdFluidTypesConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDFluidTypesExcelLoaderServiceTest() {
        try (InputStream fluidTypesSchemaIn = FSDFluidTypesLoaderProcessor.class
                .getClassLoader().getResourceAsStream(FSDFluidTypesLoaderProcessor.FLUID_TYPES_SCHEMA_JSON)) {

            assertNotNull(fluidTypesSchemaIn);

            String fluidTypesSchema = new String(fluidTypesSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdFluidTypesExcelLoaderService = new FSDFluidTypesExcelLoaderService("1", "A2", connection);
            fsdFluidTypesExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.FLUID_TYPES, fluidTypesSchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDFluidTypesTest() {
        String fileName = "ФСД_НСИ_Вид флюида.xlsx";
        try (InputStream fsdFluidTypesIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdFluidTypesIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);

            fsdFluidTypesExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdFluidTypesIn);

            Map<String, String> horizon = getDictionaryMap(connection, "horizon");
            Map<String, String> ba = getDictionaryMap(connection, "long_name", "business_associate");
            Map<String, String> field = getDictionaryMap(connection, "field");

            List<Map<String, String>> fluidTypesResults = getResultMap(connection, "fluid_types");
            assertEquals(5, fluidTypesResults.size());

            Map<String, String> fluidTypesResult = fluidTypesResults.get(0);
            assertEquals(field.get("Медвежье"), fluidTypesResult.get("field_uuid"));
            assertEquals(ba.get("ООО \"Газпром добыча Надым\""), fluidTypesResult.get("ba_uuid"));
            assertEquals(horizon.get("Медвежье.Неоком"), fluidTypesResult.get("horizon_uuid"));
            assertEquals("газ сепарации", fluidTypesResult.get("type"));
        }

    }

}
