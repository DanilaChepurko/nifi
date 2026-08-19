package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.ProjectEventEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FSDProjectEventExcelLoaderService extends FSDExcelLoaderService<FSDProjectEventParserService> {

    public FSDProjectEventExcelLoaderService(String sheetNum,
                                             String formCell,
                                             Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDProjectEventParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDProjectEventParserService fsdProjectEventParserService = fsdParserService;

        componentLog.info("Creating project Event entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<ProjectEventEntity> projectEventEntities = fsdProjectEventParserService.createProjectEventEntities(sheet);
            componentLog.info("Loading project Event entities: " + projectEventEntities.size());
            loadProjectEventEntities(projectEventEntities);
        }
    }

    private void loadProjectEventEntities(List<ProjectEventEntity> projectEventEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (projectEventEntities.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO project_event(uuid,project_step_uuid,name,number_event,short_name,type_event,lvl_event,prioritization_group) " +
                "VALUES(?,?,?,?,?,?,?,?) ON CONFLICT (name) DO UPDATE " +
                "SET project_step_uuid = ?, short_name = ?, lvl_event = ?,prioritization_group = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (ProjectEventEntity projectEventEntity : projectEventEntities) {
                preparedStatement.setObject(1, projectEventEntity.getUuid());
                preparedStatement.setObject(2, projectEventEntity.getProjectStepUuid());
                preparedStatement.setObject(3, projectEventEntity.getName());
                preparedStatement.setObject(4, projectEventEntity.getNumberEvent());
                preparedStatement.setObject(5, projectEventEntity.getShortName());
                preparedStatement.setObject(6, projectEventEntity.getTypeEvent());
                preparedStatement.setObject(7, projectEventEntity.getLvlEvent());
                preparedStatement.setObject(8, projectEventEntity.getPrioritizationGroup());
                preparedStatement.setObject(9, projectEventEntity.getProjectStepUuid());
                preparedStatement.setObject(10, projectEventEntity.getShortName());
                preparedStatement.setObject(11, projectEventEntity.getLvlEvent());
                preparedStatement.setObject(12, projectEventEntity.getPrioritizationGroup());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}
