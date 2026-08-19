package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDProjectEventExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDProjectEventExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDProjectEventExcelLoaderService fsdProjectEventExcelLoaderService;
    private final FsdProjectEventConnectionManager connectionManager = new FsdProjectEventConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDProjectEventExcelLoaderServiceTest() {
        try (             InputStream projecttEventSchemaIn = FSDProjectEventLoaderProcessor.class
                     .getClassLoader().getResourceAsStream(FSDProjectEventLoaderProcessor.PROJECT_EVENT_SCHEMA_JSON)) {

            assertNotNull(projecttEventSchemaIn);

            String projectEventSchema = new String(projecttEventSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdProjectEventExcelLoaderService = new FSDProjectEventExcelLoaderService("1", "B1", connection);
            fsdProjectEventExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.PROJECT_EVENT, projectEventSchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDProjectEventTest() {
        String fileName = "ФСД_НСИ_ Мероприятие.xlsx";
        try (InputStream fsdProjectEventIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdProjectEventIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            String sql = "delete from project_event";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            fsdProjectEventExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdProjectEventIn);

            List<Map<String, String>> projectEventResults = getResultMap(connection, "project_event");
            assertEquals(2, projectEventResults.size());

            Map<String, String> projectEventResult = projectEventResults.get(0);
            assertEquals("051-2000005.(1.1.0.1).Астраханское.Карбон.Р.С.Скважины.НКТ",
                    projectEventResult.get("name"));
            /*assertEquals("ДКС", projectEventResult.get("type"));
            assertEquals(horizon.get("Бованенковское.Сеноман-апт"), projectEventResult.get("horizon_uuid"));
            assertEquals("Техническое перевооружение турбодетандерных агрегатов на газовых промыслах " +
                    "Бованенковского НГКМ (замена СПЧ)", projectEventResult.get("project_name"));
            assertEquals(ba.get("ООО \"Газпром добыча Надым\""), projectEventResult.get("ba_uuid"));
            assertEquals("СПЧ", projectEventResult.get("equipment_name"));
            assertEquals("051-2005523", projectEventResult.get("code"));**/
        }
    }
}
