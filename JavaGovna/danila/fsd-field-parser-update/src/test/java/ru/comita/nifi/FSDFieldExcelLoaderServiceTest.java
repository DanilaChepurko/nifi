package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDFieldExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDFieldExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDFieldExcelLoaderService fsdFieldExcelLoaderService;
    private final FsdFieldConnectionManager connectionManager = new FsdFieldConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDFieldExcelLoaderServiceTest() {
        try (InputStream fieldSchemaIn = FSDFieldLoaderProcessorU.class
                .getClassLoader().getResourceAsStream(FSDFieldLoaderProcessorU.FIELD_SCHEMA_JSON)) {

            assertNotNull(fieldSchemaIn);

            String fieldSchema = new String(fieldSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdFieldExcelLoaderService = new FSDFieldExcelLoaderService("1", "B1", connection);
            fsdFieldExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.FIELD, fieldSchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDFieldTest() {
        String fileName = "ФСД НСИ Месторождения.xlsx";
        try (InputStream fsdFieldsIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdFieldsIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            String sql = "delete from field";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            fsdFieldExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdFieldsIn);

            Map<String, String> ba = getDictionaryMap(connection, "long_name", "business_associate");
            Map<String, String> fieldTypes = getDictionaryMap(connection, "r_field_type");
            Map<String, String> areas = getDictionaryMap(connection, "area");


            List<Map<String, String>> fieldResults = getResultMap(connection, "field");
            assertEquals(1, fieldResults.size());

            Map<String, String> fieldResult = fieldResults.get(0);
            assertEquals("Астраханское", fieldResult.get("name"));
            assertEquals(areas.get("Астраханская обл."), fieldResult.get("area_uuid"));
            assertEquals(ba.get("ООО \"Газпром добыча Астрахань\""), fieldResult.get("ba_uuid"));
            assertEquals(fieldTypes.get("газоконденсатное"), fieldResult.get("type_uuid"));
            assertEquals("АСТ14553НЭ 20.08.2008", fieldResult.get("licenses"));
            assertEquals("2025", fieldResult.get("year_open"));
            assertEquals("t", fieldResult.get("esg"));
        }
    }
}
