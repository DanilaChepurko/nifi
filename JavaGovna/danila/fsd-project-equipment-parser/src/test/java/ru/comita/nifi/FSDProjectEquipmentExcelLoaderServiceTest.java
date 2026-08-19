package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDProjectEquipmentExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDProjectEquipmentExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDProjectEquipmentExcelLoaderService fsdProjectEquipmentExcelLoaderService;
    private final FsdProjectEquipmentConnectionManager connectionManager = new FsdProjectEquipmentConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDProjectEquipmentExcelLoaderServiceTest() {
        try (             InputStream projecttEquipmentSchemaIn = FSDProjectEquipmentLoaderProcessor.class
                     .getClassLoader().getResourceAsStream(FSDProjectEquipmentLoaderProcessor.PROJECT_EQUIPMENT_SCHEMA_JSON)) {

            assertNotNull(projecttEquipmentSchemaIn);

            String projectEquipmentSchema = new String(projecttEquipmentSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdProjectEquipmentExcelLoaderService = new FSDProjectEquipmentExcelLoaderService("1", "B1", connection);
            fsdProjectEquipmentExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.PROJECT_EQUIPMENT, projectEquipmentSchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDProjectEquipmentTest() {
        String fileName = "ФСД_НСИ_Оборудование.xlsx";
        try (InputStream fsdProjectEquipment = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdProjectEquipment);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            String sql = "delete from equipment";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            fsdProjectEquipmentExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdProjectEquipment);

            List<Map<String, String>> projectEquipmentResults = getResultMap(connection, "equipment");
            assertEquals(2, projectEquipmentResults.size());

            Map<String, String> projectEquipmentResult = projectEquipmentResults.get(1);
            assertEquals("привод ГПА",
                    projectEquipmentResult.get("name"));
            /*assertEquals("ДКС", projectEquipmentResult.get("type"));
            assertEquals(horizon.get("Бованенковское.Сеноман-апт"), projectEquipmentResult.get("horizon_uuid"));
            assertEquals("Техническое перевооружение турбодетандерных агрегатов на газовых промыслах " +
                    "Бованенковского НГКМ (замена СПЧ)", projectEquipmentResult.get("project_name"));
            assertEquals(ba.get("ООО \"Газпром добыча Надым\""), projectEquipmentResult.get("ba_uuid"));
            assertEquals("СПЧ", projectEquipmentResult.get("equipment_name"));
            assertEquals("051-2005523", projectEquipmentResult.get("code"));**/
        }
    }
}
