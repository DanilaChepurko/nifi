package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDPricesExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDPricesExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDPricesExcelLoaderService fsdPricesExcelLoaderService;
    private final FsdPricesConnectionManager connectionManager = new FsdPricesConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDPricesExcelLoaderServiceTest() {
        try (InputStream pricesSchemaIn = FSDPricesLoaderProcessor.class
                .getClassLoader().getResourceAsStream(FSDPricesLoaderProcessor.PRICES_SCHEMA_JSON);
                InputStream headerSchemaIn = FSDPricesLoaderProcessor.class
                        .getClassLoader().getResourceAsStream(FSDPricesLoaderProcessor.HEADER_SCHEMA_JSON)) {

            assertNotNull(pricesSchemaIn);
            assertNotNull(headerSchemaIn);

            String pricesSchema = new String(pricesSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String headerSchema = new String(headerSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdPricesExcelLoaderService = new FSDPricesExcelLoaderService("2", "B1", connection);
            fsdPricesExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.PRICES, pricesSchema);
            schemaMap.put(SchemaName.HEADER, headerSchema);

        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDPricesTest() {
        String fileName = "ФСД_Цены (по ГДО) - 1.xlsx";
        try (InputStream fsdPricesIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdPricesIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);

            fsdPricesExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdPricesIn);

            Map<String, String> scenario = getDictionaryMap(connection, "r_scenario");
            Map<String, String> analytic = getDictionaryMap(connection, "r_analytic");
            Map<String, String> ba = getDictionaryMap(connection, "long_name", "business_associate");
            Map<String, String> field = getDictionaryMap(connection, "field");

            List<Map<String, String>> pricesResults = getResultMap(connection, "gdo_prices");
            assertEquals(8, pricesResults.size());

            List<Map<String, String>> headersResults = getResultMap(connection, "header_econom");
            assertEquals(1, headersResults.size());

            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(scenario.get("ПАО \"Газпром\""), headerResult.get("scenario"));
            assertEquals(2024, Integer.parseInt(headerResult.get("year")));
            assertEquals(field.get("Медвежье"), headerResult.get("field_uuid"));
            assertEquals(ba.get("ООО \"Газпром добыча Надым\""), headerResult.get("ba_uuid"));
            assertEquals("Форма сбора данных (ФСД): Цены", headerResult.get("fsd_source"));

            Map<String, String> pricesResult = pricesResults.get(1);

            assertEquals(analytic.get("Цв (цена на газ на внутр.рынке в реальном исчисл.)(для НДПИ) руб./1000 м³"),
                    pricesResult.get("analytic_uuid"));
            assertEquals(2.00, Double.parseDouble(pricesResult.get("value")), 0.01);
            assertEquals(headerResult.get("uuid"), pricesResult.get("header_uuid"));

        }

    }

}
