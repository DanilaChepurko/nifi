package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDMacroExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDMacroExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDMacroExcelLoaderService fsdMacroExcelLoaderService;
    private final FsdMacroConnectionManager connectionManager = new FsdMacroConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDMacroExcelLoaderServiceTest() {
        try (InputStream macroSchemaIn = FSDMacroLoaderProcessorEconom.class
                .getClassLoader().getResourceAsStream(FSDMacroLoaderProcessorEconom.MACRO_SCHEMA_JSON);
             InputStream headerSchemaIn = FSDMacroLoaderProcessorEconom.class
                .getClassLoader().getResourceAsStream(FSDMacroLoaderProcessorEconom.HEADER_SCHEMA_JSON)
        ) {

            assertNotNull(headerSchemaIn);
            assertNotNull(macroSchemaIn);

            String headerSchema = new String(headerSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String macroSchema = new String(macroSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdMacroExcelLoaderService = new FSDMacroExcelLoaderService("2", "B1", connection);
            fsdMacroExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.HEADER, headerSchema);
            schemaMap.put(SchemaName.MACRO, macroSchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDMacroTest() {
        String fileName = "ФСД_Макроокруж (для всех) - 1.xlsx";
        try (InputStream fsdMacroIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdMacroIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            fsdMacroExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdMacroIn);

            Map<String, String> scenario = getDictionaryMap(connection, "r_scenario");
            Map<String, String> analytic = getDictionaryMap(connection, "r_analytic");


            List<Map<String, String>> macroResults = getResultMap(connection, "macro_tax");
            List<Map<String, String>> headerResults = getResultMap(connection, "header_econom");

            assertEquals(5, macroResults.size());
            assertEquals(1, headerResults.size());
            Map<String, String> macroResult = macroResults.get(1);
            Map<String, String> headerResult = headerResults.get(0);

            assertEquals(analytic.get("Цена нефти Brent долл./барр"), macroResult.get("analytic_uuid"));
            assertEquals(scenario.get("ЦКР"), headerResult.get("scenario"));
            assertEquals(73.00,  Double.parseDouble(macroResult.get("value")), 0.01);
            assertEquals("2024", headerResult.get("year"));
        }
    }
    }
/*
    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDMacroEmptyHorizonAreaTest() {
        String fileName = "ФСД НСИ Площадки Empty Horizon Area.xlsx";
        try (InputStream fsdMacroIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdMacroIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            String sql = "delete from macro_tax";// чистка базы
            Statement statement = connection.createStatement();
            statement.execute(sql);
            fsdMacroExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdMacroIn);

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
    }*/

