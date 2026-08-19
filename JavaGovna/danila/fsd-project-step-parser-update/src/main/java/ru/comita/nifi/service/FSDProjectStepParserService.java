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
import ru.comita.nifi.dto.entity.ProjectStepEntity;
import ru.comita.nifi.dto.schema.ProjectStepSchema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FSDProjectStepParserService extends FSDParserService {
    private ProjectStepSchema projectStepSchema;

    public FSDProjectStepParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.projectStepSchema = initSchema(schemaMap.get(SchemaName.PROJECT_STEP), form, new TypeReference<>() {
        });
    }

    public List<ProjectStepEntity> createProjectStepEntities(StreamingSheet sheet) {
        List<ProjectStepEntity> projectStepEntities = new ArrayList<>();
        if (projectStepSchema == null) {
            return projectStepEntities;
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        UUID baUuid = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                if (FsdParserUtil.isInSameRow(row, projectStepSchema.getBusinessAssociate())) {
                    baUuid = excelParserService
                            .getUUIDByCellAddress(row, "business_associate", "long_name",
                                    projectStepSchema.getBusinessAssociate());
                }
                if (row.getRowNum() + 1 >= projectStepSchema.getStartRow()) {
                    String projectStepName = excelParserService
                            .getCellValueByAddress(row, projectStepSchema.getNameColumn());
                    if (projectStepName != null) {
                        ProjectStepEntity projectStepEntity = new ProjectStepEntity();
                        projectStepEntity.setName(projectStepName);
                        projectStepEntity.setUuid(UUID.randomUUID());
                        projectStepEntity.setHorizon(excelParserService
                                .getUUIDByCellAddress(row, "horizon", projectStepSchema.getHorizonColumn()));
                        projectStepEntity.setCode(excelParserService
                                .getCellValueByAddress(row, projectStepSchema.getCodeColumn()));
                        projectStepEntity.setBa(baUuid);
                        projectStepEntity.setType(excelParserService
                                .getCellValueByAddress(row, projectStepSchema.getTypeColumn()));
                        projectStepEntity.setEquipmentName(excelParserService
                                .getCellValueByAddress(row, projectStepSchema.getEquipmentNameColumn()));
                        projectStepEntity.setProjectName(excelParserService
                                .getCellValueByAddress(row, projectStepSchema.getProjectNameColumn()));
                        projectStepEntities.add(projectStepEntity);
                    }
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return projectStepEntities;
    }
}
