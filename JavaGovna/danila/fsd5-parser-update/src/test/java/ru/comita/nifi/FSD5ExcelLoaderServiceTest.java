package ru.comita.nifi;


import lombok.SneakyThrows;

import org.antlr.runtime.misc.IntArray;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSD5ExcelLoaderService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FSD5ExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSD5ExcelLoaderService fsd5ExcelLoaderService;
    private final Fsd5ConnectionManager connectionManager = new Fsd5ConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSD5ExcelLoaderServiceTest() {
        try (InputStream headerSchemaIn = FSD5LoaderProcessor.class
                .getClassLoader().getResourceAsStream(FSD5LoaderProcessor.HEADER_SCHEMA_JSON);
             InputStream volSummarySchemaIn = FSD5LoaderProcessor.class
                     .getClassLoader().getResourceAsStream(FSD5LoaderProcessor.VOL_SUMMARY_SCHEMA_JSON)) {

            assertNotNull(headerSchemaIn);
            assertNotNull(volSummarySchemaIn);

            String headerSchema = new String(headerSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String volSummarySchema = new String(volSummarySchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsd5ExcelLoaderService = new FSD5ExcelLoaderService("2", "B1", connection);
            fsd5ExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.HEADER, headerSchema);
            schemaMap.put(SchemaName.VOL_SUMMARY, volSummarySchema);
        }
    }

    @Test
    @SneakyThrows
    public void parseFSD5_1Test() {
        String fileName = "5.1_ФСД_макс_сут_КПР.xlsm";
        try (InputStream fsd5In = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsd5In);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            fsd5ExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsd5In);

            Map<String, String> analytics = getDictionaryMap(connection, "r_analytic");
            Map<String, String> versions = getDictionaryMap(connection, "r_version");
            Map<String, String> scenarios = getDictionaryMap(connection, "r_scenario");
            Map<String, String> horizon = getDictionaryMap(connection, "horizon");
            Map<String, String> projectStep = getDictionaryMap(connection, "project_step");
            Map<String, String> typeTime = getDictionaryMap(connection, "r_period_type");
            Map<String, String> fields = getDictionaryMap(connection, "field");
            Map<String, String> ba = getDictionaryMap(connection, "long_name", "business_associate");

            
            List<Map<String, String>> headersResults = getResultMap(connection, "header_development");
            List<Map<String, String>> headersMetaResults = getResultMap(connection, "header_development_meta_inf");
            List<Map<String, String>> volSummaryResults = getResultMap(connection, "pden_vol_summary_development");
            assertEquals(1, headersResults.size());
            assertEquals(1, headersMetaResults.size());
            assertEquals(2, volSummaryResults.size());

            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(versions.get("Утвержденная"), headerResult.get("version_uuid"));
            assertEquals(2025, Integer.valueOf(headerResult.get("year")));
            assertEquals(scenarios.get("КПР"), headerResult.get("scenario"));
            assertEquals(scenarios.get("Приросты максимальной добычи"), headerResult.get("dop_scenario"));
            assertEquals(fields.get("Астраханское"), headerResult.get("field_uuid"));
            assertEquals(horizon.get("Астраханское.Карбон"), headerResult.get("horizon_uuid"));
            assertEquals("Газ+Конденсат", headerResult.get("fluid_type"));
            assertEquals(ba.get("ООО \"Газпром добыча Астрахань\""), headerResult.get("business_associate_uuid"));

            Map<String, String> headerMetaResult = headersMetaResults.get(0);
            assertEquals(headerResult.get("uuid"), headerMetaResult.get("header_uuid"));
            assertEquals("Иванов И.И.", headerMetaResult.get("author"));
            assertEquals(LocalDate.of(2026, 02, 10).toString(), headerMetaResult.get("fill_in_date"));
            assertEquals(versions.get("1.1"), headerMetaResult.get("sync_version"));
            assertEquals(versions.get("2.1"), headerMetaResult.get("sync_compared_version"));
            assertEquals(scenarios.get("КПР"), headerMetaResult.get("sync_scenario"));
            assertEquals(scenarios.get("Пиковый баланс газа"), headerMetaResult.get("sync_compared_scenario"));
            
            var volSummaryResult = volSummaryResults.get(0);
            assertEquals(1,Integer.parseInt(volSummaryResult.get("project_step_group")));
            assertEquals("Прискважинное оборудование",volSummaryResult.get("equipment_category"));
            assertEquals(2024, Integer.parseInt(volSummaryResult.get("year")));
            assertEquals(typeTime.get("quarter"), volSummaryResult.get("period_type_uuid"));
            assertEquals(projectStep.get("Реконструкция обвязки устья скважины газовой эксплуатационной № 263 УППГ-2"), volSummaryResult.get("project_step_uuid"));
            Map<String, BigDecimal> expectedByName = Map.of(
                "Прирост максимальной суточной добычи газа, млн. м3/сут", BigDecimal.valueOf(1.0),
                "Прирост максимальной суточной добычи нестабильного конденсата, тыс. т/сут", BigDecimal.valueOf(3.0)//,
               // "Прирост максимальной суточной добычи стабильного конденсата, тыс. т/сут", BigDecimal.valueOf(0.0)
            );
            
            Map<String, BigDecimal> expectedByUuid = new HashMap<>();
            for (Map.Entry<String, BigDecimal> entry : expectedByName.entrySet()) {
                String name = entry.getKey();
                String uuid = analytics.get(name);
                assertNotNull(uuid, "Аналитика с именем '" + name + "' не найдена в словаре");
                expectedByUuid.put(uuid, entry.getValue());
            }

            Map<String, BigDecimal> actualValues = new HashMap<>();
            
            for (Map<String, String> row : volSummaryResults) {
                String uuid = row.get("analytics_uuid");
                BigDecimal value = BigDecimal.valueOf(Double.parseDouble(row.get("value")));
                actualValues.put(uuid, value);
            }
            assertEquals(expectedByUuid, actualValues);
        }
    }
    /*
    @Test
    @SneakyThrows
    public void parseFSD5_3Test() {
        String fileName = "5.3_ФСД_эффекты_год_доб_КПР.xlsm";
        try (InputStream fsd5In = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsd5In);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            fsd5ExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsd5In);

            Map<String, String> analytics = getDictionaryMap(connection, "r_analytic");
            Map<String, String> versions = getDictionaryMap(connection, "r_version");
            Map<String, String> scenarios = getDictionaryMap(connection, "r_scenario");
            Map<String, String> horizon = getDictionaryMap(connection, "horizon");
            Map<String, String> projectStep = getDictionaryMap(connection, "project_step");
            Map<String, String> typeTime = getDictionaryMap(connection, "r_period_type");
            Map<String, String> fields = getDictionaryMap(connection, "field");
            Map<String, String> ba = getDictionaryMap(connection, "long_name", "business_associate");

            
            List<Map<String, String>> headersResults = getResultMap(connection, "header_development");
            List<Map<String, String>> headersMetaResults = getResultMap(connection, "header_development_meta_inf");
            List<Map<String, String>> volSummaryResults = getResultMap(connection, "pden_vol_summary_development");
            assertEquals(1, headersResults.size());
            assertEquals(1, headersMetaResults.size());
            assertEquals(2, volSummaryResults.size());

            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(versions.get("Утвержденная"), headerResult.get("version_uuid"));
            assertEquals(2025, Integer.valueOf(headerResult.get("year")));
            assertEquals(scenarios.get("КПР"), headerResult.get("scenario"));
            assertEquals(fields.get("Астраханское"), headerResult.get("field_uuid"));
            assertEquals(horizon.get("Астраханское.Карбон"), headerResult.get("horizon_uuid"));
            assertEquals("Газ", headerResult.get("fluid_type"));
            assertEquals(ba.get("ООО \"Газпром добыча Астрахань\""), headerResult.get("business_associate_uuid"));

            Map<String, String> headerMetaResult = headersMetaResults.get(0);
            assertEquals(headerResult.get("uuid"), headerMetaResult.get("header_uuid"));
            assertEquals("Иванов И.И.", headerMetaResult.get("author"));
            assertEquals(LocalDate.of(2024, 01, 01).toString(), headerMetaResult.get("fill_in_date"));
            assertEquals(versions.get("1.1"), headerMetaResult.get("sync_version"));
            assertEquals(versions.get("2.1"), headerMetaResult.get("sync_compared_version"));
            assertEquals(scenarios.get("КПР"), headerMetaResult.get("sync_scenario"));
            assertEquals(scenarios.get("Уровни добычи из АН"), headerMetaResult.get("sync_compared_scenario"));
            
            var volSummaryResult = volSummaryResults.get(0);
            assertEquals(projectStep.get("Реконструкция обвязки устья скважины газовой эксплуатационной № 263 УППГ-2"), volSummaryResult.get("project_step_uuid"));
            assertEquals(1,Integer.parseInt(volSummaryResult.get("project_step_group")));
            assertEquals("Прискважинное оборудование",volSummaryResult.get("equipment_category"));
            assertEquals(2024, Integer.parseInt(volSummaryResult.get("year")));
            assertEquals(typeTime.get("year"), volSummaryResult.get("period_type_uuid"));
            
            Map<String, BigDecimal> expectedByName = Map.of(
                "Валовый Газ, млн. м3", BigDecimal.valueOf(2.0),
                "Товарный Газ, млн. м3", BigDecimal.valueOf(3.0)
            );
            
            Map<String, BigDecimal> expectedByUuid = new HashMap<>();
            for (Map.Entry<String, BigDecimal> entry : expectedByName.entrySet()) {
                String name = entry.getKey();
                String uuid = analytics.get(name);
                assertNotNull(uuid, "Аналитика с именем '" + name + "' не найдена в словаре");
                expectedByUuid.put(uuid, entry.getValue());
            }

            Map<String, BigDecimal> actualValues = new HashMap<>();
            
            for (Map<String, String> row : volSummaryResults) {
                String uuid = row.get("analytics_uuid");
                BigDecimal value = BigDecimal.valueOf(Double.parseDouble(row.get("value")));
                actualValues.put(uuid, value);
            }
            assertEquals(expectedByUuid, actualValues);
        }
    }*/

    // @Test
    // @SneakyThrows
    // public void parseFSD5_5Test() {
    //     String fileName = "ФСД-5.5 КПР Эффекты Газовый объект.xlsx";
    //     try (InputStream fsd5In = getClass().getClassLoader().getResourceAsStream(fileName)) {
    //         assertNotNull(fsd5In);

    //         Connection connection = connectionManager.getConnection();
    //         connectionManager.initTables(connection);
    //         fsd5ExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsd5In);

    //         Map<String, String> analytics = getDictionaryMap(connection, "r_analytic");
    //         Map<String, String> versions = getDictionaryMap(connection, "r_version");
    //         Map<String, String> scenarios = getDictionaryMap(connection, "r_scenario");
    //         Map<String, String> horizon = getDictionaryMap(connection, "horizon");
    //         Map<String, String> projectStep = getDictionaryMap(connection, "project_step");
    //         Map<String, String> typeTime = getDictionaryMap(connection, "r_period_type");

    //         List<Map<String, String>> headersResults = getResultMap(connection, "header_development");
    //         List<Map<String, String>> volSummaryResults = getResultMap(connection, "pden_vol_summary_development");
    //         assertEquals(1, headersResults.size());
    //         assertEquals(15, volSummaryResults.size());

    //         Map<String, String> headerResult = headersResults.get(0);
    //         assertEquals(scenarios.get("Эффекты"), headerResult.get("scenario"));
    //         assertEquals(versions.get("Утвержденная"), headerResult.get("version_uuid"));
    //         assertEquals(versions.get("Утвержденная"), headerResult.get("version_plan_uuid"));
    //         assertEquals(dateToString(1, 2024), headerResult.get("model_date"));
    //         assertEquals(horizon.get("Бованенковское.Сеноман-апт"), headerResult.get("horizon_uuid"));
    //         assertEquals(fileName, headerResult.get("name"));

    //         AtomicReference<Integer> testPassed = new AtomicReference<>(0);
    //         volSummaryResults.forEach(volSummaryResult -> {
    //             if (analytics.get("Добыча газа с реконструкцией, млн. м3").equals(volSummaryResult.get("analytics_uuid"))) {
    //                 assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
    //                 if (projectStep.get("Тех. пер. ГПА 051-2005692.Бованенковское.Сеноман-апт.Замена ГТУ.ДКС ГП-1, 1 ГТУ")
    //                         .equals(volSummaryResult.get("project_step_uuid"))) {
    //                     if (assertTimeAndValue(volSummaryResult, typeTime.get("quarter"), 10, 2025, "562")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     } else if (assertTimeAndValue(volSummaryResult, typeTime.get("year"), 1, 2037, "903")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     }
    //                 } else if (projectStep.get("Тех. пер. ДКС 051-2006513.Бованенковское.Сеноман-апт.Замена СПЧ.ДКС ГП-1, ГП-2, 8 СПЧ")
    //                         .equals(volSummaryResult.get("project_step_uuid"))) {
    //                     if (assertTimeAndValue(volSummaryResult, typeTime.get("month"), 2, 2026, "540")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     } else if (assertTimeAndValue(volSummaryResult, typeTime.get("quarter"), 1, 2027, "521")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     } else if (assertTimeAndValue(volSummaryResult, typeTime.get("year"), 1, 2037, "903")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     }
    //                 }
    //             } else if (analytics.get("Добыча газа без реконструкциеи, млн. м3").equals(volSummaryResult.get("analytics_uuid"))) {
    //                 assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
    //                 assertEquals(horizon.get("Комсомольское.Сеноман"), volSummaryResult.get("horizon_uuid"));
    //                 if (projectStep.get("Тех. пер. ГПА 051-2005692.Бованенковское.Сеноман-апт.Замена ГТУ.ДКС ГП-1, 1 ГТУ")
    //                         .equals(volSummaryResult.get("project_step_uuid"))) {
    //                     if (assertTimeAndValue(volSummaryResult, typeTime.get("quarter"), 10, 2025, "494.56")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     } else if (assertTimeAndValue(volSummaryResult, typeTime.get("year"), 1, 2037, "442.47")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     }
    //                 } else if (projectStep.get("Тех. пер. ДКС 051-2006513.Бованенковское.Сеноман-апт.Замена СПЧ.ДКС ГП-1, ГП-2, 8 СПЧ")
    //                         .equals(volSummaryResult.get("project_step_uuid"))) {
    //                     if (assertTimeAndValue(volSummaryResult, typeTime.get("month"), 2, 2026, "453.6")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     } else if (assertTimeAndValue(volSummaryResult, typeTime.get("quarter"), 1, 2027, "432.43")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     } else if (assertTimeAndValue(volSummaryResult, typeTime.get("year"), 1, 2037, "442.47")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     }
    //                 }
    //             } else if (analytics.get("Технологический эффект по добыче газа от выполненной реконструкции, млн куб. м3").equals(volSummaryResult.get("analytics_uuid"))) {
    //                 assertEquals(headerResult.get("uuid"), volSummaryResult.get("header_uuid"));
    //                 if (projectStep.get("Тех. пер. ГПА 051-2005692.Бованенковское.Сеноман-апт.Замена ГТУ.ДКС ГП-1, 1 ГТУ")
    //                         .equals(volSummaryResult.get("project_step_uuid"))) {
    //                     if (assertTimeAndValue(volSummaryResult, typeTime.get("quarter"), 10, 2025, "562")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     } else if (assertTimeAndValue(volSummaryResult, typeTime.get("year"), 1, 2037, "903")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     }
    //                 } else if (projectStep.get("Тех. пер. ДКС 051-2006513.Бованенковское.Сеноман-апт.Замена СПЧ.ДКС ГП-1, ГП-2, 8 СПЧ")
    //                         .equals(volSummaryResult.get("project_step_uuid"))) {
    //                     if (assertTimeAndValue(volSummaryResult, typeTime.get("month"), 2, 2026, "540")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     } else if (assertTimeAndValue(volSummaryResult, typeTime.get("quarter"), 1, 2027, "521")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     } else if (assertTimeAndValue(volSummaryResult, typeTime.get("year"), 1, 2037, "903")) {
    //                         testPassed.getAndSet(testPassed.get() + 1);
    //                     }
    //                 }
    //             }
    //         });
    //         assertEquals(15, testPassed.get());
    //     }
    // }

  
}
