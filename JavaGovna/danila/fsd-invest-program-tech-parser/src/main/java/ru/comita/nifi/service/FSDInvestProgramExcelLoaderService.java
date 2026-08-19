package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;

import ru.comita.nifi.dto.entity.InvestProgramEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSDInvestProgramExcelLoaderService extends FSDExcelLoaderService<FSDInvestProgramParserService> {

    public FSDInvestProgramExcelLoaderService(String sheetNum,
                                              String formCell,
                                              Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDInvestProgramParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDInvestProgramParserService fsdInvestProgramParserService = fsdParserService;

        componentLog.info("Creating Invest Program entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<InvestProgramEntity> investProgramEntities = fsdInvestProgramParserService.createInvestProgramEntityes(sheet);
            componentLog.info("Loading Invest Program entities: " + investProgramEntities.size());
            clearPreviousRecords(investProgramEntities.get(0));
            loadProjectStepEntities(investProgramEntities);
        }
    }



    private void clearPreviousRecords(InvestProgramEntity investProgramEntitie) throws SQLException {
        //connection.setAutoCommit(false);
        String sql = String.format("delete from invest_program WHERE source_ip %s ",
                investProgramEntitie.getSourceIp() == null ? "IS NULL" : "= '" + investProgramEntitie.getSourceIp()+"'"// Без кавычек для числового поля


        );
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.execute();
        }
      //  connection.setAutoCommit(true);
    }
    private void loadProjectStepEntities(List<InvestProgramEntity> investProgramEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (investProgramEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO invest_program(uuid,field_uuid,horizon_uuid,horizon_area_uuid,source_ip," +
                "construction_namber,construction_cod,equipment_name,volume_equipment,volume_unit,start_date) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?) ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (InvestProgramEntity investProgramEntity : investProgramEntities) {
                preparedStatement.setObject(1, investProgramEntity.getUuid());
                preparedStatement.setObject(2, investProgramEntity.getField());
                preparedStatement.setObject(3, investProgramEntity.getHorizon());
                preparedStatement.setObject(4, investProgramEntity.getHorizonArea());
                preparedStatement.setObject(5, investProgramEntity.getSourceIp());
                preparedStatement.setObject(6, investProgramEntity.getConstructionNamber());
                preparedStatement.setObject(7, investProgramEntity.getConstructionCod());
                preparedStatement.setObject(8, investProgramEntity.getEquipmentName());
                preparedStatement.setObject(9, investProgramEntity.getVolumeEquipment());
                preparedStatement.setObject(10, investProgramEntity.getVolumeUnit());
                preparedStatement.setObject(11, investProgramEntity.getStartDate());

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}
