package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;
import ru.comita.nifi.dto.entity.ParamEntity;
import ru.comita.nifi.dto.entity.OpfEntity;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSDOperatingCostExcelLoaderService extends FSDExcelLoaderService<FSDOperatingCostParserService> {

    public FSDOperatingCostExcelLoaderService(String sheetNum,
                                              String formCell,
                                              Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDOperatingCostParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDOperatingCostParserService fsdOperatingCostParserService = fsdParserService;
        HeaderEntity headerEntity;
        componentLog.info("Creating header entity");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            componentLog.info("Creating header entity");

            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            headerEntity = fsdParserService.createHeaderEntity(sheet, file.getName());

            clearPreviousRecords(headerEntity);
          /*  componentLog.info("Loading header entity");
            loadHeaderEntity(headerEntity);*/
            componentLog.info("Creating param str 1 entity");

            sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<ParamEntity>paramEntites = fsdOperatingCostParserService.createParamEntities(sheet, headerEntity,1);
            componentLog.info("Create param str 1 entity"+paramEntites.size());


            componentLog.info("Creating param str 3 entity");
            sheet = (StreamingSheet) workbook.getSheetAt(sheetNum+2);
            paramEntites.addAll(fsdOperatingCostParserService.createParamEntities(sheet, headerEntity,3));
            componentLog.info("Create param str 3 entity"+paramEntites.size());


            componentLog.info("Creating param str 4 entity");
            sheet = (StreamingSheet) workbook.getSheetAt(sheetNum+3);
            paramEntites.addAll(fsdOperatingCostParserService.createParamEntities(sheet, headerEntity,4));
            componentLog.info("Create param str 4 entity"+paramEntites.size());



            componentLog.info("Creating opf entity");
            sheet = (StreamingSheet) workbook.getSheetAt(sheetNum+1);
            List<OpfEntity> opfEntities=fsdOperatingCostParserService.createOpfEntities(sheet, headerEntity);
            componentLog.info("Create opf entity"+opfEntities.size());

            clearPreviousRecords(headerEntity);
            componentLog.info("Loading header entity");
            loadHeaderEntity(headerEntity);
            componentLog.info("Loading param entity");
            loadParamEntity(paramEntites);
            componentLog.info("Loading opf entity");
            loadOpfEntity(opfEntities);

        }


        componentLog.info("Done, header_uuid: " + headerEntity.getUuid());
    }

    private void clearPreviousRecords(HeaderEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = String.format("delete from horizon_fond WHERE header_uuid IN (" +
                        "SELECT uuid FROM header_econom " +
                        "INNER JOIN horizon_econ_param ON horizon_econ_param.header_uuid = header_econom.uuid " +
                        "WHERE " +
                        "scenario %s " +
                        "AND field_uuid %s " +
                        "AND ba_uuid %s " +
                        "AND horizon_uuid %s " +
                        "AND year %s " +
                        "AND fsd_source %s)",
                headerEntity.getScenario() == null ? "IS NULL" : "= '" + headerEntity.getScenario() + "'",
                headerEntity.getFieldUuid() == null ? "IS NULL" : "= '" + headerEntity.getFieldUuid() + "'",
                headerEntity.getBaUuid() == null ? "IS NULL" : "= '" + headerEntity.getBaUuid() + "'",
                headerEntity.getHorizonUuid() == null ? "IS NULL" : "= '" + headerEntity.getHorizonUuid() + "'", // Исправлена опечатка
                headerEntity.getYear() == null ? "IS NULL" : "= " + headerEntity.getYear(),
                headerEntity.getFsdSource()==null? "IS NULL" : "= '" +headerEntity.getFsdSource()+"'"// Без кавычек для числового поля
        );
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.execute();
        }
        sql = String.format("DELETE FROM horizon_econ_param WHERE header_uuid IN (" +
                        "SELECT uuid FROM header_econom " +
                        "INNER JOIN horizon_econ_param ON horizon_econ_param.header_uuid = header_econom.uuid " +
                        "WHERE " +
                        "scenario %s " +
                        "AND field_uuid %s " +
                        "AND ba_uuid %s " +
                        "AND horizon_uuid %s " +
                        "AND year %s " +
                        "AND fsd_source %s)",
                headerEntity.getScenario() == null ? "IS NULL" : "= '" + headerEntity.getScenario() + "'",
                headerEntity.getFieldUuid() == null ? "IS NULL" : "= '" + headerEntity.getFieldUuid() + "'",
                headerEntity.getBaUuid() == null ? "IS NULL" : "= '" + headerEntity.getBaUuid() + "'",
                headerEntity.getHorizonUuid() == null ? "IS NULL" : "= '" + headerEntity.getHorizonUuid() + "'", // Исправлена опечатка
                headerEntity.getYear() == null ? "IS NULL" : "= " + headerEntity.getYear(),
                headerEntity.getFsdSource()==null? "IS NULL" : "= '" +headerEntity.getFsdSource()+"'" // Без кавычек для числового поля
        );
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.execute();
        }
        sql = String.format("delete  from header_econom WHERE uuid IN ( "+
                        "SELECT uuid FROM header_econom "+
                        "WHERE " +
                        "scenario %s " +
                        "AND field_uuid %s " +
                        "AND ba_uuid %s " +
                        "AND horizon_uuid %s " +
                        "AND year %s " +
                        "AND fsd_source %s)",
                headerEntity.getScenario() == null ? "IS NULL" : "= '" + headerEntity.getScenario() + "'",
                headerEntity.getFieldUuid() == null ? "IS NULL" : "= '" + headerEntity.getFieldUuid() + "'",
                headerEntity.getBaUuid() == null ? "IS NULL" : "= '" + headerEntity.getBaUuid() + "'",
                headerEntity.getHorizonUuid() == null ? "IS NULL" : "= '" + headerEntity.getHorizonUuid() + "'", // Исправлена опечатка
                headerEntity.getYear() == null ? "IS NULL" : "= " + headerEntity.getYear(),
                headerEntity.getFsdSource()==null? "IS NULL" : "= '" +headerEntity.getFsdSource()+"'" // Без кавычек для числового поля
        );
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.execute();
        }
    }

    private void loadHeaderEntity(HeaderEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = "INSERT INTO header_econom(uuid,year,field_uuid,ba_uuid," +
                "horizon_uuid,scenario,name,created_date,fsd_source) " +
                "VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, headerEntity.getUuid());
            preparedStatement.setObject(2, headerEntity.getYear());
            preparedStatement.setObject(3, headerEntity.getFieldUuid());
            preparedStatement.setObject(4, headerEntity.getBaUuid());
            preparedStatement.setObject(5, headerEntity.getHorizonUuid());
            preparedStatement.setObject(6, headerEntity.getScenario());
            preparedStatement.setObject(7, headerEntity.getName());

            preparedStatement.setObject(8, headerEntity.getCreatedDate());
            preparedStatement.setObject(9, headerEntity.getFsdSource());

            preparedStatement.execute();
        }
    }

    private void loadOpfEntity(List<OpfEntity> opfEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (opfEntities.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO horizon_fond(header_uuid,name,analytic_uuid,created_date,value) " +
                "VALUES(?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (OpfEntity opfEntity : opfEntities) {  // Исправлено: OpfEntity вместо ParamEntity
                preparedStatement.setObject(1, opfEntity.getHeaderUuid());
                preparedStatement.setObject(2, opfEntity.getName());
                preparedStatement.setObject(3, opfEntity.getAnalyticsUuid());
                preparedStatement.setObject(4, opfEntity.getCreatedDate());
                preparedStatement.setBigDecimal(5, opfEntity.getValue()
                        .setScale(17, RoundingMode.HALF_UP)
                        .stripTrailingZeros());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }}

        private void loadParamEntity(List<ParamEntity> paramEntities) throws SQLException {
            connection.setAutoCommit(false);
            if (paramEntities.isEmpty()) {
                return;
            }
            String sql = "INSERT INTO horizon_econ_param(header_uuid,analytic_uuid,fluid,created_date,value) " +
                    "VALUES(?,?,?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (ParamEntity paramEntity : paramEntities) {
                    preparedStatement.setObject(1, paramEntity.getHeaderUuid());
                    preparedStatement.setObject(2, paramEntity.getAnalyticsUuid());
                    preparedStatement.setObject(3, paramEntity.getFuid());
                    preparedStatement.setObject(4, paramEntity.getCreatedDate());
                    preparedStatement.setBigDecimal(5, paramEntity.getValue()
                            .setScale(17, RoundingMode.HALF_UP)
                            .stripTrailingZeros());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }}
    }

