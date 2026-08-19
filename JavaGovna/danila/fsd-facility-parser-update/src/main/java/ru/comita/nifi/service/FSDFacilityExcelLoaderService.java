package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.FacilityEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSDFacilityExcelLoaderService extends FSDExcelLoaderService<FSDFacilityParserService> {

    public FSDFacilityExcelLoaderService(String sheetNum,
                                         String formCell,
                                         Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDFacilityParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDFacilityParserService fsdFacilityParserService = fsdParserService;
        componentLog.info("Creating facility entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<FacilityEntity> facilityEntities = fsdFacilityParserService.createFacilityEntities(sheet);
            componentLog.info("Loading facility entities: " + facilityEntities.size());
            loadFacilityEntities(facilityEntities);
        }
    }

    private void loadFacilityEntities(List<FacilityEntity> facilityEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (facilityEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO facility(uuid,name,field_uuid,horizon_uuid,horizon_area_uuid,short_name, dkc) " +
                "VALUES(?,?,?,?,?,?,?) ON CONFLICT (name) DO UPDATE " +
                "SET field_uuid = ?, horizon_uuid = ?, horizon_area_uuid = ?, short_name = ?, dkc=? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (FacilityEntity facilityEntity : facilityEntities) {
                preparedStatement.setObject(1, facilityEntity.getUuid());
                preparedStatement.setObject(2, facilityEntity.getName());
                preparedStatement.setObject(3, facilityEntity.getFieldUuid());
                preparedStatement.setObject(4, facilityEntity.getHorizonUuid());
                preparedStatement.setObject(5, facilityEntity.getHorizonAreaUuid());
                preparedStatement.setObject(6, facilityEntity.getShortName());
                preparedStatement.setObject(7, facilityEntity.getDkc());
                preparedStatement.setObject(8, facilityEntity.getFieldUuid());
                preparedStatement.setObject(9, facilityEntity.getHorizonUuid());
                preparedStatement.setObject(10, facilityEntity.getHorizonAreaUuid());
                preparedStatement.setObject(11, facilityEntity.getShortName());
                preparedStatement.setObject(12, facilityEntity.getDkc());


                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

}
