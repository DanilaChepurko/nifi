package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSD2ExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FSD2ExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSD2ExcelLoaderService fsd2ExcelLoaderService;
    private final Fsd2ConnectionManager connectionManager = new Fsd2ConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSD2ExcelLoaderServiceTest() {
        try (InputStream headerSchemaIn = FSD2uLoaderProcessor.class.getClassLoader().getResourceAsStream(FSD2uLoaderProcessor.HEADER_SCHEMA_JSON);
             InputStream volSummarySchemaIn = FSD2uLoaderProcessor.class.getClassLoader().getResourceAsStream(FSD2uLoaderProcessor.VOL_SUMMARY_SCHEMA_JSON);
             InputStream optionsSchemaIn = FSD2uLoaderProcessor.class.getClassLoader().getResourceAsStream(FSD2uLoaderProcessor.OPTIONS_SCHEMA_JSON))
        {

            assertNotNull(headerSchemaIn);
            assertNotNull(volSummarySchemaIn);
            assertNotNull(optionsSchemaIn);

            String headerSchema = new String(headerSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String volSummarySchema = new String(volSummarySchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String optionsSchema = new String(optionsSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            schemaMap.put(SchemaName.HEADER, headerSchema);
            schemaMap.put(SchemaName.VOL_SUMMARY, volSummarySchema);
            schemaMap.put(SchemaName.OPTIONS, optionsSchema);
            Connection connection = connectionManager.getConnection();
            fsd2ExcelLoaderService = new FSD2ExcelLoaderService("2", "B1", connection);
            fsd2ExcelLoaderService.setComponentLog(new MockComponentLogger());
        }
    }

    @Test
    @SneakyThrows
    public void parseFSD2_1Test() {
        String fileName = "2.1_ФСД_Проектные_уровни_добычи_по_пласту.xlsm";
        try (InputStream fsd2In = getClass().getClassLoader().getResourceAsStream(fileName)) {


            assertNotNull(fsd2In);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            fsd2ExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsd2In);

            Map<String, String> analytics = getDictionaryMap(connection, "r_analytic");
            Map<String, String> versions = getDictionaryMap(connection, "r_version");
            Map<String, String> scenarios = getDictionaryMap(connection, "r_scenario");
            Map<String, String> projectStep = getDictionaryMap(connection, "project_step");
            Map<String, String> pools = getDictionaryMap(connection, "pool");
            Map<String, String> developmentMethods = getDictionaryMap(connection, "r_development_method");
            Map<String, String> businessAssociate = getDictionaryMap(connection, "long_name", "business_associate");
            Map<String, String> typeTime = getDictionaryMap(connection, "r_period_type");
            Map<String, String> equipment = getDictionaryMap(connection, "equipment");


            List<Map<String, String>> headersResults = getResultMap(connection, "header_project");
            List<Map<String, String>> volSummaryResults = getResultMap(connection, "pden_vol_summary_project");
            List<Map<String, String>> optionResults = getResultMap(connection, "pden_option");
            assertEquals(1, headersResults.size());
            assertEquals(1, optionResults.size());
            assertEquals(3, volSummaryResults.size());

            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(scenarios.get("Уровни добычи из АН"), headerResult.get("scenario"));
            assertEquals(versions.get("Утвержденный"), headerResult.get("version_uuid"));
            assertEquals("2025", headerResult.get("year"));
            assertEquals(pools.get("Астраханское.Карбон.залежь башкирского яруса C2b"), headerResult.get("pool_uuid"));
            assertEquals(developmentMethods.get("Потенциал"), headerResult.get("development_method_uuid"));
            assertEquals(businessAssociate.get("ООО \"Газпром добыча Астрахань\""), headerResult.get("ba_uuid"));
            assertEquals(dateToString(5, 2025), headerResult.get("model_date"));
            assertEquals(fileName, headerResult.get("name"));

            Map<String, String> optionsResult = optionResults.get(0);
            assertEquals(headerResult.get("uuid"), optionsResult.get("header_uuid"));
            assertEquals(projectStep.get("051-2001317.Астраханское.Карбон.Скважины"), optionsResult.get("project_step_uuid"));
            assertEquals(dateToString(1, 2025), optionsResult.get("start_date"));
            assertEquals( 2, Double.parseDouble(optionsResult.get("volume")), 0.01);
            assertEquals( "скв.", optionsResult.get("measure_unit"));
            assertEquals(typeTime.get("quarter"), optionsResult.get("period_type_uuid"));
            assertEquals(equipment.get("Новые скважины"), optionsResult.get("equipment_uuid"));

            AtomicReference<Integer> testPassed = new AtomicReference<>(0);
            volSummaryResults.forEach(volSummaryResult -> {
                if (analytics.get("Запасы.Нефти.млн. т").equals(volSummaryResult.get("analytics_uuid"))) {
                    assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
                    assertTrue(assertTimeAndValue(volSummaryResult, typeTime.get("day"), 5, 2025, "2"));
                    assertEquals(pools.get("Астраханское.Карбон.залежь башкирского яруса C2b"), volSummaryResult.get("pool_uuid"));
                    testPassed.getAndSet(testPassed.get() + 1);
                } else if (analytics.get("Начальная добыча.Нефти.млн. т").equals(volSummaryResult.get("analytics_uuid"))) {
                    assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
                    assertEquals(pools.get("Астраханское.Карбон.залежь башкирского яруса C2b"), volSummaryResult.get("pool_uuid"));
                    assertTrue(assertTimeAndValue(volSummaryResult, typeTime.get("day"), 5, 2025, "3"));
                    testPassed.getAndSet(testPassed.get() + 1);
                } else if (analytics.get("Добыча газа. сепарации.млрд. м3").equals(volSummaryResult.get("analytics_uuid"))) {
                    assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
                    assertEquals(pools.get("Астраханское.Карбон.залежь башкирского яруса C2b"), volSummaryResult.get("pool_uuid"));
                    assertTrue(assertTimeAndValue(volSummaryResult, typeTime.get("month"), 5, 2025, "2"));
                    testPassed.getAndSet(testPassed.get() + 1);
                }
            });
            assertEquals(3, testPassed.get());
        }
    }

    @Test
    @SneakyThrows
    public void parseFSD2_2Test() {
        String fileName = "2.2_ФСД_Проектные_уровни_добычи_по_УКПГ.xlsm";
        try (InputStream fsd2In = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsd2In);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            fsd2ExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsd2In);

            Map<String, String> analytics = getDictionaryMap(connection, "r_analytic");
            Map<String, String> versions = getDictionaryMap(connection, "r_version");
            Map<String, String> scenarios = getDictionaryMap(connection, "r_scenario");
            Map<String, String> projectStep = getDictionaryMap(connection, "project_step");
            Map<String, String> facilitys = getDictionaryMap(connection, "facility");
            Map<String, String> developmentMethods = getDictionaryMap(connection, "r_development_method");
            Map<String, String> businessAssociate = getDictionaryMap(connection, "long_name", "business_associate");
            Map<String, String> typeTime = getDictionaryMap(connection, "r_period_type");
            Map<String, String> equipment = getDictionaryMap(connection, "equipment");

            List<Map<String, String>> headersResults = getResultMap(connection, "header_project");
            List<Map<String, String>> volSummaryResults = getResultMap(connection, "pden_vol_summary_project");
            List<Map<String, String>> optionResults = getResultMap(connection, "pden_option");
            assertEquals(1, headersResults.size());
            assertEquals(1, optionResults.size());
            assertEquals(2, volSummaryResults.size());

            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(scenarios.get("Уровни добычи из АН"), headerResult.get("scenario"));
            assertEquals(versions.get("Утвержденный"), headerResult.get("version_uuid"));
            assertEquals("2025", headerResult.get("year"));
            assertEquals(facilitys.get("Астраханское.УППГ"), headerResult.get("facility_uuid"));
            assertEquals(developmentMethods.get("Потенциал"), headerResult.get("development_method_uuid"));
            assertEquals(businessAssociate.get("ООО \"Газпром добыча Астрахань\""), headerResult.get("ba_uuid"));
            assertEquals(dateToString(5, 2025), headerResult.get("model_date"));
            assertEquals(fileName, headerResult.get("name"));

            Map<String, String> optionsResult = optionResults.get(0);
            assertEquals(headerResult.get("uuid"), optionsResult.get("header_uuid"));
            assertEquals(projectStep.get("051-2001317.Астраханское.Карбон.Скважины"), optionsResult.get("project_step_uuid"));
            assertEquals(dateToString(2, 2026), optionsResult.get("start_date"));
            assertEquals( 5, Double.parseDouble(optionsResult.get("volume")), 0.01);
            assertEquals( "скв.", optionsResult.get("measure_unit"));
            assertEquals(typeTime.get("month"), optionsResult.get("period_type_uuid"));
            assertEquals(equipment.get("Новые скважины"), optionsResult.get("equipment_uuid"));

            AtomicReference<Integer> testPassed = new AtomicReference<>(0);
            volSummaryResults.forEach(volSummaryResult -> {
                if (analytics.get("Начальная добыча.Нефти.млн. т").equals(volSummaryResult.get("analytics_uuid"))) {
                    assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
                    assertTrue(assertTimeAndValue(volSummaryResult, typeTime.get("day"), 5, 2025, "4"));
                    assertEquals(facilitys.get("Астраханское.УППГ"), volSummaryResult.get("facility_uuid"));
                    testPassed.getAndSet(testPassed.get() + 1);
                } else if (analytics.get("Добыча газа.сухой.млрд. м3").equals(volSummaryResult.get("analytics_uuid"))) {
                    assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
                    assertEquals(facilitys.get("Астраханское.УППГ"), volSummaryResult.get("facility_uuid"));
                    assertTrue(assertTimeAndValue(volSummaryResult, typeTime.get("month"), 8, 2026, "6"));
                    testPassed.getAndSet(testPassed.get() + 1);
                }
            });
            assertEquals(2, testPassed.get());
        }
    }

    @Test
    @SneakyThrows
    public void parseFSD2_3Test() {
        String fileName = "2.3_ФСД_Проектные_уровни_добычи_по_горизонту.xlsm";
        try (InputStream fsd2In = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsd2In);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            fsd2ExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsd2In);

            Map<String, String> analytics = getDictionaryMap(connection, "r_analytic");
            Map<String, String> versions = getDictionaryMap(connection, "r_version");
            Map<String, String> scenarios = getDictionaryMap(connection, "r_scenario");
            Map<String, String> projectStep = getDictionaryMap(connection, "project_step");
            Map<String, String> horizons = getDictionaryMap(connection, "horizon");
            Map<String, String> developmentMethods = getDictionaryMap(connection, "r_development_method");
            Map<String, String> businessAssociate = getDictionaryMap(connection, "long_name", "business_associate");
            Map<String, String> typeTime = getDictionaryMap(connection, "r_period_type");
            Map<String, String> equipment = getDictionaryMap(connection, "equipment");

            List<Map<String, String>> headersResults = getResultMap(connection, "header_project");
            List<Map<String, String>> volSummaryResults = getResultMap(connection, "pden_vol_summary_project");
            List<Map<String, String>> optionResults = getResultMap(connection, "pden_option");
            assertEquals(1, headersResults.size());
            assertEquals(1, optionResults.size());
            assertEquals(2, volSummaryResults.size());

            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(scenarios.get("Уровни добычи из АН"), headerResult.get("scenario"));
            assertEquals(versions.get("Утвержденный"), headerResult.get("version_uuid"));
            assertEquals("2025", headerResult.get("year"));
            assertEquals(horizons.get("Астраханское.Карбон"), headerResult.get("horizon_uuid"));
            assertEquals(developmentMethods.get("Потенциал"), headerResult.get("development_method_uuid"));
            assertEquals(businessAssociate.get("ООО \"Газпром добыча Астрахань\""), headerResult.get("ba_uuid"));
            assertEquals(dateToString(5, 2025), headerResult.get("model_date"));
            assertEquals(fileName, headerResult.get("name"));

            Map<String, String> optionsResult = optionResults.get(0);
            assertEquals(headerResult.get("uuid"), optionsResult.get("header_uuid"));
            assertEquals(projectStep.get("051-2001317.Астраханское.Карбон.Скважины"), optionsResult.get("project_step_uuid"));
            assertEquals(dateToString(1, 2025), optionsResult.get("start_date"));
            assertEquals( 42.0, Double.parseDouble(optionsResult.get("volume")), 0.01);
            assertEquals( "скв.", optionsResult.get("measure_unit"));
            assertEquals(typeTime.get("month"), optionsResult.get("period_type_uuid"));
            assertEquals(equipment.get("Новые скважины"), optionsResult.get("equipment_uuid"));



            AtomicReference<Integer> registryTestPassed = new AtomicReference<>(0);
            volSummaryResults.forEach(volSummaryResult -> {
                if (analytics.get("Начальная добыча.Стабильного конденсата.млн. т").equals(volSummaryResult.get("analytics_uuid"))) {
                    assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
                    assertTrue(assertTimeAndValue(volSummaryResult, typeTime.get("day"), 5, 2025, "4.5"));
                    assertEquals(horizons.get("Астраханское.Карбон"), headerResult.get("horizon_uuid"));
                    registryTestPassed.getAndSet(registryTestPassed.get() + 1);
                } else if (analytics.get("Добыча газа.сухой.млрд. м3").equals(volSummaryResult.get("analytics_uuid"))) {
                    assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
                    assertEquals(horizons.get("Астраханское.Карбон"), headerResult.get("horizon_uuid"));
                    assertTrue(assertTimeAndValue(volSummaryResult, typeTime.get("quarter"), 4, 2025, "0.42"));
                    registryTestPassed.getAndSet(registryTestPassed.get() + 1);
                }
            });
            assertEquals(2, registryTestPassed.get());
        }
    }
}
