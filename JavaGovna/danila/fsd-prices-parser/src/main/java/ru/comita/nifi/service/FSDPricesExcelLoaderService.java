package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEconomEntity;
import ru.comita.nifi.dto.entity.PricesEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class FSDPricesExcelLoaderService extends FSDExcelLoaderService<FSDPricesParserService> {

    public FSDPricesExcelLoaderService(String sheetNum,
            String formCell,
            Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDPricesParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDPricesParserService FSDPricesParserService = fsdParserService;
        HeaderEconomEntity headerEntity;

        componentLog.info("Creating header_econom entity");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            headerEntity = FSDPricesParserService.createHeaderEconomEntity(sheet);
            clearPreviousRecords(headerEntity);
            componentLog.info("Loading header entitity" + headerEntity.getUuid());
            loadHeaderEntity(headerEntity);
        }

        componentLog.info("Creating prices entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<PricesEntity> pricesEntities = FSDPricesParserService.createPricesEntities(sheet, headerEntity);
            componentLog.info("Loading prices entities: " + pricesEntities.size());
            loadPricesEntities(pricesEntities);
        }
        componentLog.info("Done. header entitity" + headerEntity.getUuid());

    }

    private void loadPricesEntities(List<PricesEntity> pricesEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (pricesEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO gdo_prices(uuid,header_uuid,analytic_uuid, value) "
                +
                "VALUES(?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (PricesEntity priceEntity : pricesEntities) {
                preparedStatement.setObject(1, priceEntity.getUuid());
                preparedStatement.setObject(2, priceEntity.getHeaderUuid());
                preparedStatement.setObject(3, priceEntity.getAnalyticUuid());
                preparedStatement.setObject(4, priceEntity.getValue());
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
                "INNER JOIN gdo_prices gp ON gp.header_uuid = he.uuid " +
                "WHERE " + scenarioCond + " AND " + yearCond + " AND " + fsdSourceCond + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM gdo_prices WHERE header_uuid IN " + subquery);
            stmt.execute("DELETE FROM header_econom WHERE uuid IN " + subquery);
            connection.commit();
        } catch (RuntimeException e) {
            componentLog.error(e.getMessage());
            connection.rollback();
        }
    }

    private void loadHeaderEntity(HeaderEconomEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = "INSERT INTO public.header_econom(uuid,scenario, ba_uuid,field_uuid,year,created_date, fsd_source) " +
                "VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, headerEntity.getUuid());
            preparedStatement.setObject(2, headerEntity.getScenarioUuid());
            preparedStatement.setObject(3, headerEntity.getBaUuid());
            preparedStatement.setObject(4, headerEntity.getFieldUuid());
            preparedStatement.setObject(5, headerEntity.getYear());
            preparedStatement.setObject(6, headerEntity.getCreatedDate());
            preparedStatement.setObject(7, headerEntity.getFsdSource());
            preparedStatement.execute();
        }
    }

}
