package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDOperatingCostExcelLoaderService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDOperatingCostExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDOperatingCostExcelLoaderService fsdOperatingCostExcelLoaderService;
    private final ConnectionManager connectionManager = new FsdOperatingCostConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDOperatingCostExcelLoaderServiceTest() {
        try (InputStream headerSchemaIn = FSDOperatingCostLoaderProcessor.class
                .getClassLoader().getResourceAsStream(FSDOperatingCostLoaderProcessor.HEADER_SCHEMA_JSON);
             InputStream paramSchemaIn = FSDOperatingCostLoaderProcessor.class
                     .getClassLoader().getResourceAsStream(FSDOperatingCostLoaderProcessor.PARAM_SCHEMA_JSON);
             InputStream opfSchemaIn = FSDOperatingCostLoaderProcessor.class//поправить
                .getClassLoader().getResourceAsStream(FSDOperatingCostLoaderProcessor.OPF_SCHEMA_JSON)) {

            assertNotNull(headerSchemaIn);
            assertNotNull(paramSchemaIn);
            assertNotNull(opfSchemaIn);


            String headerSchema = new String(headerSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String paramSchema = new String(paramSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String opfSchema = new String(opfSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdOperatingCostExcelLoaderService = new FSDOperatingCostExcelLoaderService("2", "B1", connection);
            fsdOperatingCostExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.HEADER, headerSchema);
            schemaMap.put(SchemaName.HORIZON_PARAM, paramSchema);
            schemaMap.put(SchemaName.OPF, opfSchema);
        }
    }

    @Test
    @SneakyThrows
    public void parseOperatingCostTest() {
        Connection connection = connectionManager.getConnection();
        connectionManager.initTables(connection);
        String fileName = "ФСД_ЭЗ.xlsx";
        try (InputStream fsdOperatingCostIn = getClass().getClassLoader().getResourceAsStream(fileName)) {



            assertNotNull(fsdOperatingCostIn);
            fsdOperatingCostExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdOperatingCostIn);

            Map<String, String> analytics = getDictionaryMap(connection, "r_analytic");

            Map<String, String> horizon = getDictionaryMap(connection, "horizon");
            Map<String, String> field = getDictionaryMap(connection, "field");



            List<Map<String, String>> headersResults = getResultMap(connection, "header_econom");
            List<Map<String, String>> opfResults = getResultMap(connection, "horizon_fond");
            List<Map<String, String>> paramResults = getResultMap(connection, "horizon_econ_param");

            assertEquals(1, headersResults.size());
            assertEquals(8, opfResults.size());
            assertEquals(3,paramResults.size());

            Map<String, String> testParam = paramResults.get(0);

            assertEquals(Double.valueOf(3.40), Double.valueOf(testParam.get("value")));

            assertEquals(analytics.get("Затраты на ППД - газ руб./тыс. м3 руб./т"), testParam.get("analytic_uuid"));
            /*



            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(scenarios.get("Запасы геологические"), headerResult.get("scenario"));
            assertEquals("Заключение государственной экспертизы", headerResult.get("document_name"));
            assertEquals(dateToString(6, 12, 2023), headerResult.get("approval_document_date"));
            assertEquals("документ", headerResult.get("parent_document_name"));
            assertEquals(dateToString(6, 12, 2023), headerResult.get("parent_approval_document_date"));
            assertEquals(fileName, headerResult.get("name"));


            Map<String, String> materialBalResult = materialBalResults.get(1);
            assertEquals(analytics.get("Начальные геологич. запасы сухого газа (СВ), млн. м3"), materialBalResult.get("analytics_uuid"));
            assertEquals(headerResult.get("uuid"), materialBalResult.get("header_uuid"));
            assertEquals(horizon.get("Бованенковское.Сеноман-апт"), materialBalResult.get("horizon_uuid"));
            assertEquals(pool.get("Бованенковское.Сеноман-апт.I"), materialBalResult.get("pool_uuid"));
            assertEquals(horizonArea.get("Бованенковское.Сеноман-апт"), materialBalResult.get("horizon_area_uuid"));
            assertEquals("БУ8/1-2", materialBalResult.get("stratum"));
            assertEquals(stage.get("K2s"), materialBalResult.get("stage_uuid"));
            assertEquals(new BigDecimal("2").toPlainString(), new BigDecimal(materialBalResult.get("value"))
                    .stripTrailingZeros().toPlainString());*/
        }
    }
}
