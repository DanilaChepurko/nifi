package ru.comita.nifi;


import lombok.SneakyThrows;
import org.apache.nifi.mock.MockComponentLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.comita.lib.dto.SchemaName;
import ru.comita.nifi.service.FSDProjectEventEquipmentExcelLoaderService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FSDProjectEventEquipmentExcelLoaderServiceTest extends FSDExcelLoaderServiceTest {

    private final FSDProjectEventEquipmentExcelLoaderService fsdProjectEventEquipmentExcelLoaderService;
    private final FsdProjectEventEquipmentConnectionManager connectionManager = new FsdProjectEventEquipmentConnectionManager();
    private final Map<SchemaName, String> schemaMap = new HashMap<>();

    @AfterEach
    public void clear() {
        connectionManager.clearAllTables();
    }

    @SneakyThrows
    public FSDProjectEventEquipmentExcelLoaderServiceTest() {
        try (InputStream headerSchemaIn = FSDProjectEventEquipmentLoaderProcessor.class.getClassLoader().getResourceAsStream(FSDProjectEventEquipmentLoaderProcessor.HEADER_SCHEMA_JSON);
             InputStream projectEventSchemaIn = FSDProjectEventEquipmentLoaderProcessor.class.getClassLoader().getResourceAsStream(FSDProjectEventEquipmentLoaderProcessor.PROJECT_EVENT_EQUIPMENT_SCHEMA_JSON)) {

            assertNotNull(headerSchemaIn);
            assertNotNull(projectEventSchemaIn);

            String headerSchema = new String(headerSchemaIn.readAllBytes(), StandardCharsets.UTF_8);
            String projectEventSchema = new String(projectEventSchemaIn.readAllBytes(), StandardCharsets.UTF_8);

            schemaMap.put(SchemaName.HEADER, headerSchema);
            schemaMap.put(SchemaName.PROJECT_EVENT, projectEventSchema);
            Connection connection = connectionManager.getConnection();
            fsdProjectEventEquipmentExcelLoaderService = new FSDProjectEventEquipmentExcelLoaderService("1", "B1", connection);
            fsdProjectEventEquipmentExcelLoaderService.setComponentLog(new MockComponentLogger());

        }
    }

    @SuppressWarnings("SqlWithoutWhere")
    @Test
    @SneakyThrows
    public void parseFSDProjectEventTest() {
        String fileName = "ФСД_Оборудование.xlsx";
        try (InputStream fsdProjectEventIn = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(fsdProjectEventIn);

            Connection connection = connectionManager.getConnection();
            connectionManager.initTables(connection);
            fsdProjectEventEquipmentExcelLoaderService.loadExcelToTargetDatabase(fileName, schemaMap, fsdProjectEventIn);
            List<Map<String, String>> headersResults = getResultMap(connection, "header_econom");
            List<Map<String, String>> equipmentInUse = getResultMap(connection, "equipment_in_use");

            Map<String, String> version = getDictionaryMap(connection, "r_version");
            Map<String, String> equipment = getDictionaryMap(connection, "equipment");
            Map<String, String> projectEventvent = getDictionaryMap(connection, "project_event");



            assertEquals(1, headersResults.size());
            assertEquals(2, equipmentInUse.size());

            Map<String, String> headerResult = headersResults.get(0);

            assertEquals(version.get("Вариант №1"), headerResult.get("version_uuid"));

            assertEquals("1", headerResult.get("iteration"));

            assertEquals(fileName, headerResult.get("name"));

            assertEquals("2025", headerResult.get("year"));
           // assertEquals(LocalDateTime.of(2025,2,10,0,0), headerResult.get("created_date"));


            Map<String, String> equipmentInUseRez = equipmentInUse.get(1);

            assertEquals(headerResult.get("uuid"), equipmentInUseRez.get("header_uuid"));
            assertEquals(equipment.get("ГПА"), equipmentInUseRez.get("equipment_uuid"));
            assertEquals(projectEventvent.get("051-2000006.(1.1.0.1).Астраханское.Карбон.Р.П.ДКС.Привод ГПА"), equipmentInUseRez.get("project_event_uuid"));
            assertEquals("2026", equipmentInUseRez.get("year"));
            assertEquals("агр.", equipmentInUseRez.get("measure_unit"));
            assertEquals(1.0, Double.parseDouble( equipmentInUseRez.get("physical_volume")));
            assertEquals(425.0, Double.parseDouble(equipmentInUseRez.get("estimated_cost")));
            assertEquals("Обустройство месторождения", equipmentInUseRez.get("functional_group"));




        }
    }
}
