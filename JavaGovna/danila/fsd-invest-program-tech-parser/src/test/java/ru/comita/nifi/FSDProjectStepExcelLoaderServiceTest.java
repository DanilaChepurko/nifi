package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDInvestProgramExcelLoaderService;

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

    private final FSDInvestProgramExcelLoaderService fsdInvestProgramExcelLoaderService;
    private final FsdProjectStepConnectionManager connectionManager = new FsdProjectStepConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDProjectStepExcelLoaderServiceTest() {
        try (InputStream projectStepSchemaIn = FSDInvestProgramLoaderProcessor.class
                     .getClassLoader().getResourceAsStream(FSDInvestProgramLoaderProcessor.INVEST_PROGRAM_SCHEMA_JSON)) {

            assertNotNull(projectStepSchemaIn);

            String projectStepSchema = new String(projectStepSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdInvestProgramExcelLoaderService = new FSDInvestProgramExcelLoaderService("1", "B1", connection);
            fsdInvestProgramExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.HEADER, projectStepSchema);
        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDProjectStepTest() {
        String fileName = "ФСД-2.ИП.xlsx";
        try (InputStream fsdProjectStepIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdProjectStepIn);

             Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            //String sql = "delete from project_step";
            //Statement statement = connection.createStatement();
            //statement.execute(sql);*/
            fsdInvestProgramExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdProjectStepIn);

            Map<String, String> field = getDictionaryMap(connection, "field");
            Map<String, String> horizon = getDictionaryMap(connection, "horizon");
            Map<String, String> horizonArea = getDictionaryMap(connection, "horizon_area");

            List<Map<String, String>> projectStepResults = getResultMap(connection, "invest_program");
            assertEquals(2, projectStepResults.size());

            Map<String, String> projectStepResult = projectStepResults.get(0);
            assertEquals(field.get("Уренгойское"), projectStepResult.get("field_uuid"));
            assertEquals(horizon.get("Уренгойское.Сеноман"), projectStepResult.get("horizon_uuid"));
            assertEquals(horizonArea.get("Уренгойское.Сеноман.Песцовая"), projectStepResult.get("horizon_area_uuid"));
            assertEquals("Д644 2026 г.",
                    projectStepResult.get("source_ip"));
            assertEquals("1", projectStepResult.get("construction_namber"));
            assertEquals("051-3001052", projectStepResult.get("construction_cod"));
            assertEquals("Боковой ствол", projectStepResult.get("equipment_name"));
            assertEquals("11.0", projectStepResult.get("volume_equipment"));
            assertEquals("ед.", projectStepResult.get("volume_unit"));
            assertEquals(dateToString(10, 2030), projectStepResult.get("start_date"));
        }
    }
}
