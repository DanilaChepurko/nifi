package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEconomEntity;
import ru.comita.nifi.dto.entity.TaxesEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FSDTaxesExcelLoaderService extends FSDExcelLoaderService<FSDTaxesParserService> {

    public FSDTaxesExcelLoaderService(String sheetNum,
            String formCell,
            Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDTaxesParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDTaxesParserService FSDTaxesParserService = fsdParserService;
        HeaderEconomEntity headerEntity;
        String type;

        componentLog.info("Creating header_econom entity");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            headerEntity = FSDTaxesParserService.createHeaderEconomEntity(sheet);
            clearPreviousRecords(headerEntity);
            type=headerEntity.getFsdSource();
            componentLog.info("Loading header entitity" + headerEntity.getUuid());
            loadHeaderEntity(headerEntity);
        }

        componentLog.info("Creating taxes entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            if ("Форма сбора данных (ФСД): Федеральные налоги".equals(type)){
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<TaxesEntity> taxesEntities = FSDTaxesParserService.createTaxesEntities(sheet, headerEntity, true);
            componentLog.info("Loading taxes entities: " + taxesEntities.size());
            loadTaxesEntities(taxesEntities);
            } else if ("Форма сбора данных (ФСД): НДПИ".equals(type)) {
                StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
                List<TaxesEntity> taxesEntities = FSDTaxesParserService.createTaxesEntities(sheet, headerEntity, true);
                componentLog.info("Loading taxes entities in 1 list: " + taxesEntities.size());
                sheet=(StreamingSheet) workbook.getSheetAt(sheetNum+1);
                taxesEntities.addAll(FSDTaxesParserService.createTaxesEntities(sheet, headerEntity, false));
                loadTaxesEntities(taxesEntities);

            }

        }
        componentLog.info("Done. header entitity" + headerEntity.getUuid());

    }

    private void loadTaxesEntities(List<TaxesEntity> taxesEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (taxesEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO tax_indicator(uuid,header_uuid,analytic_uuid,year, value) "
                +
                "VALUES(?,?,?,?,?)";
        // "ON CONFLICT (analytic_uuid,header_econom) DO UPDATE " +
        // "SET value = ?, year = ?"
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (TaxesEntity taxEntity : taxesEntities) {
                preparedStatement.setObject(1, taxEntity.getUuid());
                preparedStatement.setObject(2, taxEntity.getHeaderUuid());
                preparedStatement.setObject(3, taxEntity.getAnalyticUuid());
                preparedStatement.setObject(4, taxEntity.getYear());
                preparedStatement.setObject(5, taxEntity.getValue());
                // preparedStatement.setObject(6, taxEntity.getValue());
                // preparedStatement.setObject(7, taxEntity.getYear());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private void clearPreviousRecords(HeaderEconomEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String scenarioCond = headerEntity.getScenarioUuid() == null
                ? "he.scenario IS NULL"
                : "he.scenario = '" + headerEntity.getScenarioUuid() + "'";

        String yearCond = headerEntity.getYear() == null
                ? "he.year IS NULL"
                : "he.year = " + headerEntity.getYear();

        String fsdSourceCond = headerEntity.getFsdSource() == null
                ? "he.fsd_source IS NULL"
                : "he.fsd_source = '" + headerEntity.getFsdSource() + "'";

        String subquery = "(SELECT he.uuid FROM header_econom he " +
                "INNER JOIN tax_indicator ti ON ti.header_uuid = he.uuid " +
                "WHERE " + scenarioCond + " AND " + yearCond + " AND " + fsdSourceCond + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM tax_indicator WHERE header_uuid IN " + subquery);
            stmt.execute("DELETE FROM header_econom WHERE uuid IN " + subquery);
            connection.commit();
        } catch (RuntimeException e) {
            componentLog.error(e.getMessage());
            connection.rollback();
        }
    }

    private void loadHeaderEntity(HeaderEconomEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = "INSERT INTO public.header_econom(uuid,scenario,year,created_date,fsd_source) " +
                "VALUES(?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, headerEntity.getUuid());
            preparedStatement.setObject(2, headerEntity.getScenarioUuid());
            preparedStatement.setObject(3, headerEntity.getYear());
            preparedStatement.setObject(4, headerEntity.getCreatedDate());
            preparedStatement.setObject(5, headerEntity.getFsdSource());
            preparedStatement.execute();
        }
    }

}
