package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDTaxesExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDTaxesExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDTaxesExcelLoaderService fsdTaxesExcelLoaderService;
    private final FsdTaxesConnectionManager connectionManager = new FsdTaxesConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDTaxesExcelLoaderServiceTest() {
        try (InputStream taxesSchemaIn = FSDTaxesLoaderProcessor.class
                .getClassLoader().getResourceAsStream(FSDTaxesLoaderProcessor.TAXES_SCHEMA_JSON);
                InputStream headerSchemaIn = FSDTaxesLoaderProcessor.class
                        .getClassLoader().getResourceAsStream(FSDTaxesLoaderProcessor.HEADER_SCHEMA_JSON)) {

            assertNotNull(taxesSchemaIn);
            assertNotNull(headerSchemaIn);

            String taxesSchema = new String(taxesSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String headerSchema = new String(headerSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdTaxesExcelLoaderService = new FSDTaxesExcelLoaderService("2", "B1", connection);
            fsdTaxesExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.TAXES, taxesSchema);
            schemaMap.put(SchemaName.HEADER, headerSchema);

        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDTaxesTest() {
        String fileName = "ФСД_Налоги (для всех).xlsx";
        try (InputStream fsdTaxesIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdTaxesIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            String sql = "delete from tax_indicator";// чистка базы
            Statement statement = connection.createStatement();
            statement.execute(sql);
            fsdTaxesExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdTaxesIn);

            Map<String, String> scenario = getDictionaryMap(connection, "r_scenario");
            Map<String, String> analytic = getDictionaryMap(connection, "r_analytic");

            List<Map<String, String>> taxesResults = getResultMap(connection, "tax_indicator");
            assertEquals(10, taxesResults.size());

            List<Map<String, String>> headersResults = getResultMap(connection, "header_econom");
            assertEquals(1, headersResults.size());

            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(scenario.get("ПАО \"Газпром\""), headerResult.get("scenario"));
            assertEquals(2024, Integer.parseInt(headerResult.get("year")));

            Map<String, String> taxesResult = taxesResults.get(1);

            assertEquals(analytic.get("Налог на имущество %"), taxesResult.get("analytic_uuid"));
            assertEquals(2.20, Double.parseDouble(taxesResult.get("value")), 0.01);
            assertEquals(headerResult.get("uuid"), taxesResult.get("header_uuid"));
            assertEquals("Форма сбора данных (ФСД): Федеральные налоги", headerResult.get("fsd_source"));
/*
            Map<String, String> combinedTaxesResult = taxesResults.get(3);
            assertEquals(analytic.get(
                    "Взносы на социальное страхование Предельная величина базы для исчисления страховых взносов тыс.руб./чел."),
                    combinedTaxesResult.get("analytic_uuid"));
            assertEquals(2225.00, Double.parseDouble(combinedTaxesResult.get("value")), 0.01);
            assertEquals(headerResult.get("uuid"), combinedTaxesResult.get("header_uuid"));

            Map<String, String> secondCombinedTaxesResult = taxesResults.get(6);
            assertEquals(analytic.get(
                    "Страхование от несчастных случаев на производстве газ, газоконденсат %"),
                    secondCombinedTaxesResult.get("analytic_uuid"));
            assertEquals(0.2, Double.parseDouble(secondCombinedTaxesResult.get("value")), 0.01);
            assertEquals(headerResult.get("uuid"), secondCombinedTaxesResult.get("header_uuid"));*/

        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDTaxesTestNDPI() {
        String fileName = "ФСД_Налоги (НДПИ_НК).xlsx";
        try (InputStream fsdTaxesIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdTaxesIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);

            fsdTaxesExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap,
                    fsdTaxesIn);

            Map<String, String> scenario = getDictionaryMap(connection, "r_scenario");
            Map<String, String> analytic = getDictionaryMap(connection, "r_analytic");

            List<Map<String, String>> taxesResults = getResultMap(connection,
                    "tax_indicator");
            assertEquals(25, taxesResults.size());

            List<Map<String, String>> headersResults = getResultMap(connection,
                    "header_econom");
            assertEquals(1, headersResults.size());

            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(scenario.get("ПАО \"Газпром\""), headerResult.get("scenario"));
            assertEquals(2024, Integer.parseInt(headerResult.get("year")));

            Map<String, String> taxesResult = taxesResults.get(1);

            assertEquals(analytic.get("Автомобильный бензин АИ-92 ЦАБвр"), taxesResult.get("analytic_uuid"));
            assertEquals(2025, Integer.parseInt(taxesResult.get("year")));
            assertEquals(2.00, Double.parseDouble(taxesResult.get("value")), 0.01);
            assertEquals(headerResult.get("uuid"), taxesResult.get("header_uuid"));

            Map<String, String> secondTaxesResult = taxesResults.get(12);
            assertEquals(analytic.get("Дизельное топливо ЦДТвр"), secondTaxesResult.get("analytic_uuid"));
            assertEquals(2024, Integer.parseInt(secondTaxesResult.get("year")));
            assertEquals(13.00, Double.parseDouble(secondTaxesResult.get("value")), 0.01);
            assertEquals(headerResult.get("uuid"),
                    secondTaxesResult.get("header_uuid"));

        }
    }

}
