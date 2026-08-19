package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;
import ru.comita.nifi.dto.entity.MacroEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSDMacroExcelLoaderService extends FSDExcelLoaderService<FSDMacroParserService> {

    public FSDMacroExcelLoaderService(String sheetNum,
                                      String formCell,
                                      Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDMacroParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDMacroParserService fsdMacroParserService = fsdParserService;
        componentLog.info("Creating macro entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            componentLog.info("Creating header entities");

            HeaderEntity headerEntities = fsdMacroParserService.createHeaderEntity(sheet, file.getName());
            componentLog.info("Loading header entities: " );
            clearPreviousRecords(headerEntities);
            loadHeaderEntity(headerEntities);

            componentLog.info("Creating macro entities");

            List<MacroEntity> macroEntities = fsdMacroParserService.createMacroEntities(sheet,headerEntities);
            componentLog.info("Loading macro entities: " + macroEntities.size());
            loadMacroEntities(macroEntities);
        }
    }

    private void clearPreviousRecords(HeaderEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = String.format("delete from macro_tax WHERE header_uuid IN (" +
                        "SELECT header_econom.uuid FROM header_econom " +
                        "INNER JOIN macro_tax ON macro_tax.header_uuid = header_econom.uuid " +
                        "WHERE " +
                        "scenario %s " +
                        "AND field_uuid %s " +
                        "AND ba_uuid %s " +
                        "AND horizon_uuid %s " +
                        "AND year %s " +
                        "AND fsd_source %s )",
                headerEntity.getScenario() == null ? "IS NULL" : "= '" + headerEntity.getScenario() + "'",
                headerEntity.getFieldUuid() == null ? "IS NULL" : "= '" + headerEntity.getFieldUuid() + "'",
                headerEntity.getBaUuid() == null ? "IS NULL" : "= '" + headerEntity.getBaUuid() + "'",
                headerEntity.getHorizonUuid() == null ? "IS NULL" : "= '" + headerEntity.getHorizonUuid() + "'", // Исправлена опечатка
                headerEntity.getYear() == null ? "IS NULL" : "= " + headerEntity.getYear(),
                headerEntity.getFsdSource() == null ? "IS NULL" : "= '" + headerEntity.getFsdSource() + "'" // Без кавычек для числового поля
// Без кавычек для числового поля
        );
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.execute();
        }

        sql = String.format("delete  from header_econom WHERE uuid IN ( "+
                        "SELECT header_econom.uuid FROM header_econom "+
                        "WHERE " +
                        "scenario %s " +
                        "AND field_uuid %s " +
                        "AND ba_uuid %s " +
                        "AND horizon_uuid %s " +
                        "AND year %s " +
                        "AND fsd_source %s )",
                headerEntity.getScenario() == null ? "IS NULL" : "= '" + headerEntity.getScenario() + "'",
                headerEntity.getFieldUuid() == null ? "IS NULL" : "= '" + headerEntity.getFieldUuid() + "'",
                headerEntity.getBaUuid() == null ? "IS NULL" : "= '" + headerEntity.getBaUuid() + "'",
                headerEntity.getHorizonUuid() == null ? "IS NULL" : "= '" + headerEntity.getHorizonUuid() + "'", // Исправлена опечатка
                headerEntity.getYear() == null ? "IS NULL" : "= " + headerEntity.getYear(),
                headerEntity.getFsdSource() == null ? "IS NULL" : "= '" + headerEntity.getFsdSource() + "'" // Без кавычек для числового поля
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




    private void loadMacroEntities(List<MacroEntity> macroEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (macroEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO macro_tax (uuid,header_uuid,analytic_uuid, value, created_date) " +
                "VALUES(?,?,?,?,?) ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (MacroEntity macroEntity : macroEntities) {
                preparedStatement.setObject(1, macroEntity.getUuid());
                preparedStatement.setObject(2, macroEntity.getHeaderUuid());
                preparedStatement.setObject(3, macroEntity.getAnalyticUuid());
                preparedStatement.setObject(4, macroEntity.getValue());
                preparedStatement.setObject(5, macroEntity.getCreatedDate());



                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

}
