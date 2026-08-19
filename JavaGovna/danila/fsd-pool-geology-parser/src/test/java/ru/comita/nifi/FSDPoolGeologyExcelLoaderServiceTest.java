package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDPoolGeologyExcelLoaderService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDPoolGeologyExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDPoolGeologyExcelLoaderService fsdPoolGeologyExcelLoaderService;
    private final FsdPoolGeologyConnectionManager connectionManager = new FsdPoolGeologyConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDPoolGeologyExcelLoaderServiceTest() {
        try (InputStream poolGeologySchemaIn = FSDPoolGeologyLoaderProcessor.class
                .getClassLoader().getResourceAsStream(FSDPoolGeologyLoaderProcessor.POOL_GEOLOGY_SCHEMA_JSON)) {

            assertNotNull(poolGeologySchemaIn);

            String poolGeologySchema = new String(poolGeologySchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdPoolGeologyExcelLoaderService = new FSDPoolGeologyExcelLoaderService("1", "A2", connection);
            fsdPoolGeologyExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.POOL_GEOLOGY, poolGeologySchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDPoolGeologyTest() {
        String fileName = "ФСД_НСИ_Геология месторождения.xlsx";
        try (InputStream fsdPoolGeologyIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdPoolGeologyIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);

            fsdPoolGeologyExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdPoolGeologyIn);

            Map<String, String> horizon = getDictionaryMap(connection, "horizon");
            Map<String, String> ba = getDictionaryMap(connection, "long_name", "business_associate");
            Map<String, String> field = getDictionaryMap(connection, "field");
            Map<String, String> pool = getDictionaryMap(connection, "pool");
            Map<String, String> area = getDictionaryMap(connection, "area");

            List<Map<String, String>> poolGeologyResults = getResultMap(connection, "pool_geology");
            assertEquals(2, poolGeologyResults.size());

            Map<String, String> poolGeologyResult = poolGeologyResults.get(0);

            assertEquals(field.get("Медвежье"), poolGeologyResult.get("field_uuid"));
            assertEquals(ba.get("ООО \"Газпром добыча Надым\""), poolGeologyResult.get("ba_uuid"));
            assertEquals(horizon.get("Медвежье.Неоком"), poolGeologyResult.get("horizon_uuid"));
            assertEquals(pool.get("Медвежье.Неоком.I"), poolGeologyResult.get("pool_uuid"));
            assertEquals(area.get("иное расположение"), poolGeologyResult.get("area_uuid"));
            assertEquals(2100.0, Double.valueOf(poolGeologyResult.get("depth")));
            assertEquals("f", poolGeologyResult.get("low_perm_thin"));
            assertEquals("f", poolGeologyResult.get("low_perm_thick"));

        }

    }

}
