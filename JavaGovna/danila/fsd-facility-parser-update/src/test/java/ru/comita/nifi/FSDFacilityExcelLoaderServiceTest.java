package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDFacilityExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDFacilityExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDFacilityExcelLoaderService fsdFacilityExcelLoaderService;
    private final FsdFacilitiesConnectionManager connectionManager = new FsdFacilitiesConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDFacilityExcelLoaderServiceTest() {
        try (InputStream facilitySchemaIn = FSDFacilityLoaderProcessorU.class
                .getClassLoader().getResourceAsStream(FSDFacilityLoaderProcessorU.FACILITY_SCHEMA_JSON)) {

            assertNotNull(facilitySchemaIn);

            String facilitySchema = new String(facilitySchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdFacilityExcelLoaderService = new FSDFacilityExcelLoaderService("1", "B1", connection);
            fsdFacilityExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.FACILITY, facilitySchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDFacilityTest() {
        String fileName = "ФСД НСИ Площадки.xlsx";
        try (InputStream fsdFacilitiesIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdFacilitiesIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            String sql = "delete from facility";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            fsdFacilityExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdFacilitiesIn);

            Map<String, String> horizon = getDictionaryMap(connection, "horizon");
            Map<String, String> horizonArea = getDictionaryMap(connection, "horizon_area");
            Map<String, String> fields = getDictionaryMap(connection, "field");

            List<Map<String, String>> facilityResults = getResultMap(connection, "facility");
            assertEquals(1, facilityResults.size());

            Map<String, String> facilityResult = facilityResults.get(0);
            assertEquals("Уренгойское.Ачимовский.Участок 1А.УКПГ-31", facilityResult.get("name"));
            assertEquals("УКПГ-31", facilityResult.get("short_name"));
            assertEquals(fields.get("Уренгойское"), facilityResult.get("field_uuid"));
            assertEquals(horizon.get("Уренгойское.Ачимовский"), facilityResult.get("horizon_uuid"));
            assertEquals(horizonArea.get("Уренгойское.Ачимовский.Участок 1А"), facilityResult.get("horizon_area_uuid"));
            assertEquals("Тест", facilityResult.get("dkc"));
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDFacilityEmptyHorizonAreaTest() {
        String fileName = "ФСД НСИ Площадки Empty Horizon Area.xlsx";
        try (InputStream fsdFacilitiesIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdFacilitiesIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            String sql = "delete from facility";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            fsdFacilityExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdFacilitiesIn);

            Map<String, String> horizon = getDictionaryMap(connection, "horizon");
            Map<String, String> fields = getDictionaryMap(connection, "field");

            List<Map<String, String>> facilityResults = getResultMap(connection, "facility");
            assertEquals(1, facilityResults.size());

            Map<String, String> facilityResult = facilityResults.get(0);
            assertEquals("Уренгойское.Ачимовский.УКПГ-31", facilityResult.get("name"));
            assertEquals("УКПГ-31", facilityResult.get("short_name"));
            assertEquals(fields.get("Уренгойское"), facilityResult.get("field_uuid"));
            assertEquals(horizon.get("Уренгойское.Ачимовский"), facilityResult.get("horizon_uuid"));
            assertEquals(null, facilityResult.get("dkc"));

        }
    }
}
