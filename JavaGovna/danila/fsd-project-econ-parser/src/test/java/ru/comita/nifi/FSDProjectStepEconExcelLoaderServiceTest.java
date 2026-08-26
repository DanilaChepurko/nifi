package ru.comita.nifi;

import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDProjectEconExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDProjectStepEconExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDProjectEconExcelLoaderService fsdProjectEconExcelLoaderService;
    private final FsdProjectStepEconConnectionManager connectionManager = new FsdProjectStepEconConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDProjectStepEconExcelLoaderServiceTest() {
        try (InputStream projectEconSchemaIn = FSDProjectEconLoaderProcessor.class
                .getClassLoader().getResourceAsStream(FSDProjectEconLoaderProcessor.PROJECT_ECON_SCHEMA_JSON);
                InputStream headerSchemaIn = FSDProjectEconLoaderProcessor.class
                        .getClassLoader().getResourceAsStream(FSDProjectEconLoaderProcessor.HEADER_SCHEMA_JSON)) {

            assertNotNull(projectEconSchemaIn);
            assertNotNull(headerSchemaIn);

            String projectEconSchema = new String(projectEconSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String headerSchema = new String(headerSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            Connection connection = connectionManager.getConnection();
            fsdProjectEconExcelLoaderService = new FSDProjectEconExcelLoaderService("1", "B1", connection);
            fsdProjectEconExcelLoaderService.setComponentLog(new MockComponentLogger());
            schemaMap.put(SchemaName.PROJECT_ECON, projectEconSchema);
            schemaMap.put(SchemaName.HEADER, headerSchema);

        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDProjectEconTest() {
        String fileName = "ФСД_ИП+стройка.xlsx";
        try (InputStream fsdProjectEconIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdProjectEconIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);

            fsdProjectEconExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdProjectEconIn);

            Map<String, String> analytic = getDictionaryMap(connection, "r_analytic");
            Map<String, String> field = getDictionaryMap(connection, "name", "field");
            Map<String, String> project = getDictionaryMap(connection, "project_step");
            Map<String, String> scenario = getDictionaryMap(connection, "r_scenario");

            List<Map<String, String>> projectEconResults = getResultMap(connection, "project_step_econ");
            assertEquals(2, projectEconResults.size());

            List<Map<String, String>> headersResults = getResultMap(connection, "header_econom");
            assertEquals(1, headersResults.size());

            Map<String, String> headerResult = headersResults.get(0);
            assertEquals(field.get("Ямбургское"), headerResult.get("field_uuid"));
            assertEquals(2025, Integer.parseInt(headerResult.get("year")));

            Map<String, String> projectEconResult = projectEconResults.get(1);

            assertEquals(analytic.get("Капитальные вложения, млн руб."),
                    projectEconResult.get("analytic_uuid"));
            assertEquals(1227.00, Double.parseDouble(projectEconResult.get("value")), 0.01);
            assertEquals(headerResult.get("uuid"), projectEconResult.get("header_uuid"));
            assertEquals(2029, Integer.parseInt(projectEconResult.get("smr_end_year")));
            assertEquals(2028, Integer.parseInt(projectEconResult.get("smr_start_year")));
            assertEquals(2026, Integer.parseInt(projectEconResult.get("pir_start_year")));
            assertEquals(2027, Integer.parseInt(projectEconResult.get("pir_end_year")));
            assertEquals(null, projectEconResult.get("project_dependency"));
            assertEquals("Не критическая", projectEconResult.get("priority"));
            assertEquals("2025-2029", projectEconResult.get("complex_reconstruction_program"));
            assertEquals("2027", projectEconResult.get("project_step_completion_year"));

            assertEquals("Скважины", projectEconResult.get("functional_group"));
            assertEquals("Новое строительство", projectEconResult.get("construction_type"));
            assertEquals(scenario.get("Сценарий 2"), projectEconResult.get("r_scenario"));
            assertEquals("2025-2029", projectEconResult.get("invest_program"));

            Map<String, String> projectEconResult2 = projectEconResults.get(0);
            assertEquals(project.get("051-3001236.Чаядинское.Карбон.Скважины.Новые скважины"), projectEconResult2.get("project_dependency"));

        }

    }

}
