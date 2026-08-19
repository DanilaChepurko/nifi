package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;
import ru.comita.nifi.dto.entity.OptionEntity;
import ru.comita.nifi.dto.entity.VolSummaryEntity;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSD2ExcelLoaderService extends FSDExcelLoaderService<FSD2ParserService> {

    public FSD2ExcelLoaderService(String sheetNum,
                                  String formCell,
                                  Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSD2ParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSD2ParserService fsd2ParserService = fsdParserService;
        HeaderEntity headerEntity;
        componentLog.info("Creating header entity");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            headerEntity = fsd2ParserService.createHeaderEntity(sheet, file.getName());

            clearPreviousRecords(headerEntity);
            componentLog.info("Loading header entity");
            loadHeaderEntity(headerEntity);
        }

        componentLog.info("Creating vol summary entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum + 1);
            List<VolSummaryEntity> volSummaryEntities = fsd2ParserService.createVolSummaryEntities(sheet, headerEntity);
            componentLog.info("Loading vol summary entities: " + volSummaryEntities.size());
            loadVolSummaryEntities(volSummaryEntities);
        }

        componentLog.info("Creating additional vol summary entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<VolSummaryEntity> volSummaryEntities = fsd2ParserService.createVolSummaryFromAdditionalAnalytics(sheet, headerEntity);
            componentLog.info("Loading additional vol summary entities: " + volSummaryEntities.size());
            loadVolSummaryEntities(volSummaryEntities);
        }

        componentLog.info("Creating option entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<OptionEntity> optionEntities = fsd2ParserService.createOptionEntities(sheet, headerEntity);
            componentLog.info("Loading option entities: " + optionEntities.size());
            loadOptionEntities(optionEntities);
        }
        componentLog.info("Done, header_uuid: " + headerEntity.getUuid());
    }

    private void clearPreviousRecords(HeaderEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = String.format("SELECT uuid from header_project where " +
                        "scenario %s " +
                        "and version_uuid %s " +
                        "and development_method_uuid %s " +
                        "and pool_uuid %s " +
                        "and facility_uuid %s " +
                        "and year %s " +
                        "and horizon_uuid %s",
                headerEntity.getScenario() == null ? "is null" : "= '" + headerEntity.getScenario() + "'",
                headerEntity.getVersionUuid() == null ? "is null" : "= '" + headerEntity.getVersionUuid() + "'",
                headerEntity.getDevelopmentMethodUuid() == null ? "is null" : "= '" + headerEntity.getDevelopmentMethodUuid() + "'",
                headerEntity.getPoolUuid() == null ? "is null" : "= '" + headerEntity.getPoolUuid() + "'",
                headerEntity.getFacilityUuid() == null ? "is null" : "= '" + headerEntity.getFacilityUuid() + "'",
                headerEntity.getYear() == null ? "is null" : "= '" + headerEntity.getYear() + "'",
                headerEntity.getHorizon() == null ? "is null" : "= '" + headerEntity.getHorizon() + "'");
        clearHeaderAndReferences(sql, "header_project", List.of("pden_vol_summary_project", "pden_option","recom"));
    }

    private void loadHeaderEntity(HeaderEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = "INSERT INTO header_project(uuid,scenario,year,version_uuid,name,created_date,updated_date," +
                "pool_uuid,facility_uuid,development_method_uuid,ba_uuid,model_date,horizon_uuid) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, headerEntity.getUuid());
            preparedStatement.setObject(2, headerEntity.getScenario());
            preparedStatement.setObject(3, headerEntity.getYear());
            preparedStatement.setObject(4, headerEntity.getVersionUuid());
            preparedStatement.setString(5, headerEntity.getName());
            preparedStatement.setObject(6, headerEntity.getCreatedDate());
            preparedStatement.setObject(7, headerEntity.getUpdatedDate());
            preparedStatement.setObject(8, headerEntity.getPoolUuid());
            preparedStatement.setObject(9, headerEntity.getFacilityUuid());
            preparedStatement.setObject(10, headerEntity.getDevelopmentMethodUuid());
            preparedStatement.setObject(11, headerEntity.getBaUuid());
            preparedStatement.setObject(12, headerEntity.getModelDate());
            preparedStatement.setObject(13, headerEntity.getHorizon());
            preparedStatement.execute();
        }
    }

    private void loadVolSummaryEntities(List<VolSummaryEntity> volSummaryEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (volSummaryEntities.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO pden_vol_summary_project(header_uuid,facility_uuid,pool_uuid," +
                "analytics_uuid,period_type_uuid,start_date,value,horizon_uuid) " +
                "VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (VolSummaryEntity volSummaryEntity : volSummaryEntities) {
                preparedStatement.setObject(1, volSummaryEntity.getHeaderUuid());
                preparedStatement.setObject(2, volSummaryEntity.getFacilityUuid());
                preparedStatement.setObject(3, volSummaryEntity.getPoolUuid());
                preparedStatement.setObject(4, volSummaryEntity.getAnalyticsUuid());
                preparedStatement.setObject(5, volSummaryEntity.getPeriodTypeUuid());
                preparedStatement.setObject(6, volSummaryEntity.getStartDate());
                preparedStatement.setBigDecimal(7, volSummaryEntity.getValue()
                        .setScale(17, RoundingMode.HALF_UP)
                        .stripTrailingZeros());
                preparedStatement.setObject(8, volSummaryEntity.getHorizonUuid());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    // добовление колоннки volume
    private void loadOptionEntities(List<OptionEntity> optionEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (optionEntities.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO pden_option(header_uuid,project_step_uuid,period_type_uuid,start_date,volume, measure_unit,coment,equipment_uuid) " +
                "VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (OptionEntity optionEntity : optionEntities) {
                preparedStatement.setObject(1, optionEntity.getHeaderUuid());
                preparedStatement.setObject(2, optionEntity.getProjectStepUuid());
                preparedStatement.setObject(3, optionEntity.getPeriodTypeUuid());
                preparedStatement.setObject(4, optionEntity.getStartDate());
                preparedStatement.setObject(5, optionEntity.getVolume());
                preparedStatement.setObject(6, optionEntity.getEdIzmer());
                preparedStatement.setObject(7, optionEntity.getComent());
                preparedStatement.setObject(8,optionEntity.getProjectEquipment());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}
