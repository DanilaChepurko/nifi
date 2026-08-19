package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.FieldEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSDFieldExcelLoaderService extends FSDExcelLoaderService<FSDFieldParserService> {

    public FSDFieldExcelLoaderService(String sheetNum,
                                      String formCell,
                                      Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDFieldParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDFieldParserService fsdFieldParserService = fsdParserService;
        componentLog.info("Creating field entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<FieldEntity> fieldEntities = fsdFieldParserService.createFieldEntities(sheet);
            componentLog.info("Loading field entities: " + fieldEntities.size());
            loadFieldEntities(fieldEntities);
        }
    }

    private void loadFieldEntities(List<FieldEntity> fieldEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (fieldEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO field(uuid,name,ba_uuid,area_uuid,type_uuid,licenses,esg,year_open, product_sale_uuid) " +
                "VALUES(?,?,?,?,?,?,?,?,?) ON CONFLICT (name) DO UPDATE " +
                "SET ba_uuid = ?, type_uuid = ?, licenses = ?, esg = ?, year_open =? , product_sale_uuid=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (FieldEntity fieldEntity : fieldEntities) {
                preparedStatement.setObject(1, fieldEntity.getUuid());
                preparedStatement.setObject(2, fieldEntity.getName());
                preparedStatement.setObject(3, fieldEntity.getBaUuid());
                preparedStatement.setObject(4, fieldEntity.getAreaUuid());
                preparedStatement.setObject(5, fieldEntity.getTypeUuid());
                preparedStatement.setObject(6, fieldEntity.getLicenses());
                preparedStatement.setObject(7, fieldEntity.getEsg());
                preparedStatement.setObject(8, fieldEntity.getYearOpen());
                preparedStatement.setObject(9, fieldEntity.getProductSaleUuid());
                preparedStatement.setObject(10, fieldEntity.getBaUuid());
                preparedStatement.setObject(11, fieldEntity.getTypeUuid());
                preparedStatement.setObject(12, fieldEntity.getLicenses());
                preparedStatement.setObject(13, fieldEntity.getEsg());
                preparedStatement.setObject(14, fieldEntity.getYearOpen());
                preparedStatement.setObject(15, fieldEntity.getProductSaleUuid());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

}
