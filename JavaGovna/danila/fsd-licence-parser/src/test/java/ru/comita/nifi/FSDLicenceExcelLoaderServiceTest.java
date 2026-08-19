package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDLicenceExcelLoaderService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDLicenceExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDLicenceExcelLoaderService fsdLicenseExcelLoaderService;
    private final FsdLicenceConnectionManager connectionManager = new FsdLicenceConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDLicenceExcelLoaderServiceTest() {
        try (InputStream licenceSchemaIn = FSDLicenceLoaderProcessor.class
                .getClassLoader().getResourceAsStream(FSDLicenceLoaderProcessor.LICENСE_SCHEMA_JSON)) {

            assertNotNull(licenceSchemaIn);

            String licenceSchema = new String(licenceSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdLicenseExcelLoaderService = new FSDLicenceExcelLoaderService("1", "A2", connection);
            fsdLicenseExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.LICENCE, licenceSchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDLicenseTest() {
        String fileName = "ФСД_НСИ_Лицензия.xlsx";
        try (InputStream fsdLicenseIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdLicenseIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);

            fsdLicenseExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdLicenseIn);

            Map<String, String> horizon = getDictionaryMap(connection, "horizon");
            Map<String, String> ba = getDictionaryMap(connection, "long_name", "business_associate");
            Map<String, String> field = getDictionaryMap(connection, "field");
            Map<String, String> pool = getDictionaryMap(connection, "pool");

            List<Map<String, String>> licenceResults = getResultMap(connection, "horizon_document");
            assertEquals(2, licenceResults.size());

            Map<String, String> licenceResult = licenceResults.get(0);

            assertEquals(field.get("Медвежье"), licenceResult.get("field_uuid"));
            assertEquals(ba.get("ООО \"Газпром добыча Надым\""), licenceResult.get("ba_uuid"));
            assertEquals(horizon.get("Медвежье.Неоком"), licenceResult.get("horizon_uuid"));
            assertEquals(pool.get("Медвежье.Неоком.I"), licenceResult.get("pool_uuid"));
            assertEquals(2008, Integer.parseInt(licenceResult.get("licence_year")));
            assertEquals("СЛХ 02043 НЭ", licenceResult.get("licence_code"));

            Map<String, String> subsoilArea = getDictionaryMap(connection, "subsoil_area");
            List<Map<String, String>> licenceDopInfoResults = getResultMap(connection, "horizon_document_dop_info");
            assertEquals(2, licenceDopInfoResults.size());

            Map<String, String> dopInfoResult = licenceDopInfoResults.get(1);
            assertEquals(licenceResults.get(1).get("uuid"), dopInfoResult.get("horizon_document_uuid"));
            assertEquals(2002, Integer.parseInt(dopInfoResult.get("dev_start_year")));
            assertEquals(1995, Integer.parseInt(dopInfoResult.get("turon_1pct_year")));
            assertEquals(1996, Integer.parseInt(dopInfoResult.get("oil_license_year")));
            assertEquals(1997, Integer.parseInt(dopInfoResult.get("oil_geo_license_year")));
            assertEquals(1998, Integer.parseInt(dopInfoResult.get("oil_1pct_year")));
            assertEquals(Double.valueOf(0.7535), Double.parseDouble(dopInfoResult.get("oil_recovery_2011")));
            assertEquals(Double.valueOf(546.454), Double.parseDouble(dopInfoResult.get("oil_kkan_limit")));
            assertEquals(2021, Integer.parseInt(dopInfoResult.get("oil_kkan_last_year")));
            assertEquals(subsoilArea.get("иное расположение"), dopInfoResult.get("subsoil_area_uuid"));

        }

    }

}
