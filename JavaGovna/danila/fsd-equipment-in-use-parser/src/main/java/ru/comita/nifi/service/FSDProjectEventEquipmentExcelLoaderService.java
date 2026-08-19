package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;
import ru.comita.nifi.dto.entity.ProjectEventEquipmentEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSDProjectEventEquipmentExcelLoaderService extends FSDExcelLoaderService<FSDProjectEventEquipmentParserService> {

    public FSDProjectEventEquipmentExcelLoaderService(String sheetNum,
                                                      String formCell,
                                                      Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDProjectEventEquipmentParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDProjectEventEquipmentParserService fsdProjectEventEquipmentParserService = fsdParserService;

        componentLog.info("Start parsing");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            componentLog.info("Start parsing Header econ");
            HeaderEntity headerEntity= fsdProjectEventEquipmentParserService.createHeaderEntity(sheet,file.getName());
            componentLog.info("End parsing Header econ");
            componentLog.info("clean  Header econ");
            clearPreviousRecords(headerEntity);
            componentLog.info("Start parsing project event equipment econ");

            List<ProjectEventEquipmentEntity> projectEventEntities = fsdProjectEventEquipmentParserService.createProjectEventEntities(sheet,headerEntity);

            componentLog.info("Loading project Header entities: ");
            loadHeaderEntity(headerEntity);
            componentLog.info("Loading project Event entities: " + projectEventEntities.size());
            projectEventEquipmentEntities(projectEventEntities);
        }
        componentLog.info("End parsing");
    }
    private void clearPreviousRecords(HeaderEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = String.format("delete from equipment_in_use WHERE header_uuid IN (" +
                        "SELECT header_econom.uuid FROM header_econom " +
                        "INNER JOIN equipment_in_use ON equipment_in_use.header_uuid = header_econom.uuid " +
                        "WHERE " +
                        "header_econom.year %s " +
                        "AND header_econom.field_uuid %s " +
                        "AND header_econom.ba_uuid %s " +
                        "AND header_econom.horizon_uuid %s " +
                        "AND  header_econom.scenario %s " +
                        "AND header_econom.version_uuid %s " +
                        "AND header_econom.iteration %s " +
                        "AND fsd_source %s)",
                headerEntity.getYear() == null ? "IS NULL" : "= " + headerEntity.getYear() ,// Без кавычек для числового поля
                headerEntity.getFieldUuid() == null ? "IS NULL" : "= '" + headerEntity.getFieldUuid() + "'",
                headerEntity.getBaUuid() == null ? "IS NULL" : "= '" + headerEntity.getBaUuid() + "'",
                headerEntity.getHorizonUuid() == null ? "IS NULL" : "= '" + headerEntity.getHorizonUuid() + "'",
                headerEntity.getScenario() == null ? "IS NULL" : "= '" + headerEntity.getScenario() + "'",
                headerEntity.getVersion() == null ? "IS NULL" : "= '" + headerEntity.getVersion() + "'",
                headerEntity.getIteration() == null ? "IS NULL" : "= '" + headerEntity.getIteration() + "'",
                headerEntity.getFsdSource() == null ? "IS NULL" : "= '" + headerEntity.getFsdSource() + "'"


        );
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.execute();
        }
        sql = String.format("delete  from header_econom WHERE uuid IN ( "+
                        "SELECT header_econom.uuid FROM header_econom "+
                        "WHERE " +
                        "header_econom.year %s " +
                        "AND header_econom.field_uuid %s " +
                        "AND header_econom.ba_uuid %s " +
                        "AND header_econom.horizon_uuid %s " +
                        "AND  header_econom.scenario %s " +
                        "AND header_econom.version_uuid %s " +
                        "AND header_econom.iteration %s " +
                        "AND fsd_source %s)",
                headerEntity.getYear() == null ? "IS NULL" : "= " + headerEntity.getYear() ,// Без кавычек для числового поля
                headerEntity.getFieldUuid() == null ? "IS NULL" : "= '" + headerEntity.getFieldUuid() + "'",
                headerEntity.getBaUuid() == null ? "IS NULL" : "= '" + headerEntity.getBaUuid() + "'",
                headerEntity.getHorizonUuid() == null ? "IS NULL" : "= '" + headerEntity.getHorizonUuid() + "'",
                headerEntity.getScenario() == null ? "IS NULL" : "= '" + headerEntity.getScenario() + "'",
                headerEntity.getVersion() == null ? "IS NULL" : "= '" + headerEntity.getVersion() + "'",
                headerEntity.getIteration() == null ? "IS NULL" : "= '" + headerEntity.getIteration() + "'",
                headerEntity.getFsdSource() == null ? "IS NULL" : "= '" + headerEntity.getFsdSource() + "'"
                // Без кавычек для числового поля
        );
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.execute();
        }
    }

    private void loadHeaderEntity(HeaderEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = "INSERT INTO header_econom(uuid,year,field_uuid,ba_uuid," +
                "horizon_uuid,scenario,name,created_date,version_uuid,iteration,fsd_source) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, headerEntity.getUuid());
            preparedStatement.setObject(2, headerEntity.getYear());
            preparedStatement.setObject(3, headerEntity.getFieldUuid());
            preparedStatement.setObject(4, headerEntity.getBaUuid());
            preparedStatement.setObject(5, headerEntity.getHorizonUuid());
            preparedStatement.setObject(6, headerEntity.getScenario());
            preparedStatement.setObject(7, headerEntity.getName());
            preparedStatement.setObject(8, headerEntity.getCreatedDate());
            preparedStatement.setObject(9, headerEntity.getVersion());
            preparedStatement.setObject(10, headerEntity.getIteration());
            preparedStatement.setObject(11, headerEntity.getFsdSource());



            preparedStatement.execute();
        }
    }

    private void projectEventEquipmentEntities(List<ProjectEventEquipmentEntity> projectEventEquipmentEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (projectEventEquipmentEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO equipment_in_use(uuid,header_uuid,project_event_uuid,equipment_uuid,year,measure_unit,physical_volume" +
                ",estimated_cost,functional_group) " +
                "VALUES(?,?,?,?,?,?,?,?,?) ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (ProjectEventEquipmentEntity projectEventEquipmentEntity : projectEventEquipmentEntities) {
                preparedStatement.setObject(1, projectEventEquipmentEntity.getUuid());
                preparedStatement.setObject(2, projectEventEquipmentEntity.getHeaderEconUuid());
                preparedStatement.setObject(3, projectEventEquipmentEntity.getProjectEventUuid());
                preparedStatement.setObject(4, projectEventEquipmentEntity.getProjectEquipmentUuid());
                preparedStatement.setObject(5, projectEventEquipmentEntity.getYear());
                preparedStatement.setObject(6, projectEventEquipmentEntity.getMeasureUnit());
                preparedStatement.setObject(7, projectEventEquipmentEntity.getPhysicalVolume());
                preparedStatement.setObject(8, projectEventEquipmentEntity.getEstimatedCost());
                preparedStatement.setObject(9, projectEventEquipmentEntity.getFunctionalGroup());

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}
