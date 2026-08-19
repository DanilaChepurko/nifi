package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.ProjectEquipmentEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSDProjectEquipmentExcelLoaderService extends FSDExcelLoaderService<FSDProjectEquipmentParserService> {

    public FSDProjectEquipmentExcelLoaderService(String sheetNum,
                                                 String formCell,
                                                 Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDProjectEquipmentParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDProjectEquipmentParserService fsdProjectEquipmentParserService = fsdParserService;

        componentLog.info("Creating project Equipment entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<ProjectEquipmentEntity> projectEquipmentEntities = fsdProjectEquipmentParserService.createProjectEquipmentEntities(sheet);
            componentLog.info("Loading project Equipment entities:  " + projectEquipmentEntities.size());
            loadProjectEquipmentEntities(projectEquipmentEntities);
        }
    }

    private void loadProjectEquipmentEntities(List<ProjectEquipmentEntity> projectEquipmentEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (projectEquipmentEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO equipment(uuid,name,term_bu,term_nu,deprication_premium ,term_expenses,commissioning) " +
                "VALUES(?,?,?,?,?,?,?) ON CONFLICT (name) DO UPDATE " +
                "SET term_bu = ?, term_nu = ?, deprication_premium  = ?, term_expenses = ?, commissioning = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (ProjectEquipmentEntity projectEquipmentEntity : projectEquipmentEntities) {
                preparedStatement.setObject(1, projectEquipmentEntity.getUuid());
                preparedStatement.setObject(2, projectEquipmentEntity.getName());
                preparedStatement.setObject(3, projectEquipmentEntity.getTermBu());
                preparedStatement.setObject(4, projectEquipmentEntity.getTermNu());
                preparedStatement.setObject(5, projectEquipmentEntity.getDepricationPremium());
                preparedStatement.setObject(6, projectEquipmentEntity.getTermExpenses());
                preparedStatement.setObject(7, projectEquipmentEntity.getCommissioning());
                preparedStatement.setObject(8, projectEquipmentEntity.getTermBu());
                preparedStatement.setObject(9, projectEquipmentEntity.getTermNu());
                preparedStatement.setObject(10, projectEquipmentEntity.getDepricationPremium());
                preparedStatement.setObject(11, projectEquipmentEntity.getTermExpenses());
                preparedStatement.setObject(12, projectEquipmentEntity.getCommissioning());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}
