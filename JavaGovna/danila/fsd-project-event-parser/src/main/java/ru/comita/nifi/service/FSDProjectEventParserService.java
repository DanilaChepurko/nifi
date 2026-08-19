package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.ProjectEventEntity;
import ru.comita.nifi.dto.schema.ProjectEventSchema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FSDProjectEventParserService extends FSDParserService {
    private ProjectEventSchema projectEventSchema;

    public FSDProjectEventParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.projectEventSchema = initSchema(schemaMap.get(SchemaName.PROJECT_EVENT), form, new TypeReference<>() {
        });
    }

    public List<ProjectEventEntity> createProjectEventEntities(StreamingSheet sheet) {
        List<ProjectEventEntity> projectEventEntities = new ArrayList<>();
        if (projectEventSchema == null) {
            return projectEventEntities;
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                if (row.getRowNum() + 1 >= projectEventSchema.getStartRow()) {
                    String projectEventName = excelParserService
                            .getCellValueByAddress(row, projectEventSchema.getNameColumn());
                    if (projectEventName != null) {
                        ProjectEventEntity projectEventEntity = new ProjectEventEntity();
                        projectEventEntity.setName(projectEventName);
                        projectEventEntity.setUuid(UUID.randomUUID());
                        projectEventEntity.setProjectStepUuid(excelParserService
                                .getUUIDByCellAddress(row, "project_step", projectEventSchema.getProjectStepColumn()));

                        projectEventEntity.setNumberEvent(excelParserService
                                .getCellValueByAddress(row, projectEventSchema.getNumberEventColumn()));
                        projectEventEntity.setShortName(excelParserService
                                .getCellValueByAddress(row, projectEventSchema.getShortNameColumn()));
                        projectEventEntity.setTypeEvent(excelParserService
                                .getCellValueByAddress(row, projectEventSchema.getTypeEventColumn()));

                        projectEventEntity.setLvlEvent(excelParserService
                                .getCellValueByAddress(row, projectEventSchema.getLvlEventColumn()));
                        projectEventEntity.setPrioritizationGroup(excelParserService
                                .getCellValueByAddress(row, projectEventSchema.getPrioritizationGroupColumn()));

                        projectEventEntities.add(projectEventEntity);
                    }
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return projectEventEntities;
    }
}
