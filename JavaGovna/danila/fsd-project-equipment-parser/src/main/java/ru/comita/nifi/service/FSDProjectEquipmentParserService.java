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
import ru.comita.nifi.dto.entity.ProjectEquipmentEntity;
import ru.comita.nifi.dto.schema.ProjectEquipmentSchema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FSDProjectEquipmentParserService extends FSDParserService {
    private ProjectEquipmentSchema projectEquipmentSchema;

    public FSDProjectEquipmentParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.projectEquipmentSchema = initSchema(schemaMap.get(SchemaName.PROJECT_EQUIPMENT), form, new TypeReference<>() {
        });
    }

    public List<ProjectEquipmentEntity> createProjectEquipmentEntities(StreamingSheet sheet) {
        List<ProjectEquipmentEntity> projectEventEntities = new ArrayList<>();
        if (projectEquipmentSchema == null) {
            return projectEventEntities;
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                if (row.getRowNum() + 1 >= projectEquipmentSchema.getStartRow()) {
                    String projectEventName = excelParserService
                            .getCellValueByAddress(row, projectEquipmentSchema.getNameColumn());
                    if (projectEventName != null) {
                        ProjectEquipmentEntity projectEquipmentEntity = new ProjectEquipmentEntity();
                        projectEquipmentEntity.setName(projectEventName);
                        projectEquipmentEntity.setUuid(UUID.randomUUID());
                        projectEquipmentEntity.setTermBu(excelParserService
                                .getBigDecimalValue(row, projectEquipmentSchema.getTermBuColumn()));
                        projectEquipmentEntity.setTermNu(excelParserService
                                .getBigDecimalValue(row, projectEquipmentSchema.getTermNuColumn()));

                        projectEquipmentEntity.setDepricationPremium(excelParserService
                                .getBigDecimalValue(row, projectEquipmentSchema.getDepricationPremiumColumn()));
                        projectEquipmentEntity.setTermExpenses(excelParserService
                                .getBigDecimalValue(row, projectEquipmentSchema.getTermExpensesColumn()));
                        projectEquipmentEntity.setCommissioning(excelParserService
                                .getCellValueByAddress(row, projectEquipmentSchema.getCommissioningColumn()));

                        projectEventEntities.add(projectEquipmentEntity);
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
