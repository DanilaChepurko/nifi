package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.ProjectStepEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSDProjectStepExcelLoaderService extends FSDExcelLoaderService<FSDProjectStepParserService> {

    public FSDProjectStepExcelLoaderService(String sheetNum,
                                            String formCell,
                                            Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDProjectStepParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDProjectStepParserService fsdProjectStepParserService = fsdParserService;

        componentLog.info("Creating project step entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<ProjectStepEntity> projectStepEntities = fsdProjectStepParserService.createProjectStepEntities(sheet);
            componentLog.info("Loading project step entities: " + projectStepEntities.size());
            loadProjectStepEntities(projectStepEntities);
        }
    }

    private void loadProjectStepEntities(List<ProjectStepEntity> projectStepEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (projectStepEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO project_step(uuid,name,type,horizon_uuid,code,ba_uuid,equipment_name,project_name) " +
                "VALUES(?,?,?,?,?,?,?,?) ON CONFLICT (name) DO UPDATE " +
                "SET type = ?, horizon_uuid = ?, code = ?,ba_uuid = ?,equipment_name = ?,project_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (ProjectStepEntity projectStepEntity : projectStepEntities) {
                preparedStatement.setObject(1, projectStepEntity.getUuid());
                preparedStatement.setObject(2, projectStepEntity.getName());
                preparedStatement.setObject(3, projectStepEntity.getType());
                preparedStatement.setObject(4, projectStepEntity.getHorizon());
                preparedStatement.setObject(5, projectStepEntity.getCode());
                preparedStatement.setObject(6, projectStepEntity.getBa());
                preparedStatement.setObject(7, projectStepEntity.getEquipmentName());
                preparedStatement.setObject(8, projectStepEntity.getProjectName());
                preparedStatement.setObject(9, projectStepEntity.getType());
                preparedStatement.setObject(10, projectStepEntity.getHorizon());
                preparedStatement.setObject(11, projectStepEntity.getCode());
                preparedStatement.setObject(12, projectStepEntity.getBa());
                preparedStatement.setObject(13, projectStepEntity.getEquipmentName());
                preparedStatement.setObject(14, projectStepEntity.getProjectName());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}
