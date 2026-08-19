package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDProjectStepExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDProjectStepExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDProjectStepExcelLoaderService fsdProjectStepExcelLoaderService;
    private final FsdProjectStepConnectionManager connectionManager = new FsdProjectStepConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDProjectStepExcelLoaderServiceTest() {
        try (             InputStream projectStepSchemaIn = FSDProjectStepLoaderProcessorU.class
                     .getClassLoader().getResourceAsStream(FSDProjectStepLoaderProcessorU.PROJECT_STEP_SCHEMA_JSON)) {

            assertNotNull(projectStepSchemaIn);

            String projectStepSchema = new String(projectStepSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdProjectStepExcelLoaderService = new FSDProjectStepExcelLoaderService("1", "B1", connection);
            fsdProjectStepExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.PROJECT_STEP, projectStepSchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDProjectStepTest() {
        String fileName = "ФСД Мероприятия Надым синтетика.xlsx";
        try (InputStream fsdProjectStepIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdProjectStepIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            String sql = "delete from project_step";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            fsdProjectStepExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdProjectStepIn);

            Map<String, String> ba = getDictionaryMap(connection, "long_name", "business_associate");
            Map<String, String> horizon = getDictionaryMap(connection, "horizon");

            List<Map<String, String>> projectStepResults = getResultMap(connection, "project_step");
            assertEquals(1, projectStepResults.size());

            Map<String, String> projectStepResult = projectStepResults.get(0);
            assertEquals("051-2005523.Бованенковское.Сеноман-апт.ДКС.СПЧ",
                    projectStepResult.get("name"));
            assertEquals("ДКС", projectStepResult.get("type"));
            assertEquals(horizon.get("Бованенковское.Сеноман-апт"), projectStepResult.get("horizon_uuid"));
            assertEquals("Техническое перевооружение турбодетандерных агрегатов на газовых промыслах " +
                    "Бованенковского НГКМ (замена СПЧ)", projectStepResult.get("project_name"));
            assertEquals(ba.get("ООО \"Газпром добыча Надым\""), projectStepResult.get("ba_uuid"));
            assertEquals("СПЧ", projectStepResult.get("equipment_name"));
            assertEquals("051-2005523", projectStepResult.get("code"));
        }
    }
}
