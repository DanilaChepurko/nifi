package ru.comita.nifi.service;
import ru.comita.lib.exception.ExcelParserException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;
import ru.comita.nifi.dto.entity.ProjectEventEquipmentEntity;
import ru.comita.nifi.dto.schema.HeaderSchema;
import ru.comita.nifi.dto.schema.ProjectEventEquipmentSchema;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static ru.comita.lib.util.FsdParserUtil.parseStringToInt;

public class FSDProjectEventEquipmentParserService extends FSDParserService {
    private ProjectEventEquipmentSchema projectEventEquipmentSchema;
    private HeaderSchema headerSchema;

    public FSDProjectEventEquipmentParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.headerSchema = initSchema(schemaMap.get(SchemaName.HEADER), form, new TypeReference<>() {
        });
        this.projectEventEquipmentSchema = initSchema(schemaMap.get(SchemaName.PROJECT_EVENT), form, new TypeReference<>() {
        });
    }
    public HeaderEntity createHeaderEntity(StreamingSheet sheet, String fileName) {
        HeaderEntity headerEntity = new HeaderEntity();
        headerEntity.setUuid(UUID.randomUUID());
        headerEntity.setName(fileName);
        headerEntity.setFsdSource(headerSchema.getFsdSource());
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if ((row.getRowNum() + 1) > headerSchema.getLastRow()) {
                break;
            }
            try {


                if (FsdParserUtil.isInSameRow(row, headerSchema.getYear())) {
                    String yearValue = excelParserService.getCellValueByAddress(row, headerSchema.getYear());
                    if (yearValue != null ) {

                        headerEntity.setYear((int) Double.parseDouble(yearValue));
                    }
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getBusinessAssociate())) {
                    UUID BaUuid = excelParserService.getUUIDByCellAddress(row, "business_associate", "long_name", headerSchema.getBusinessAssociate());

                    if (BaUuid != null) {
                        headerEntity.setBaUuid(BaUuid);
                    } else {

                        throw new ExcelParserException("Не удалось определить ГДО", headerSchema.getBusinessAssociate());
                    }
                }

                if (FsdParserUtil.isInSameRow(row, headerSchema.getField())) {

                    UUID Field = excelParserService.getUUIDByCellAddress(row, "field", headerSchema.getField());

                    if (Field != null) {
                        headerEntity.setFieldUuid(Field);
                    } else {
                        throw new ExcelParserException("Не удалось определить месторождение", headerSchema.getField());
                    }
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getVersion())) {
                    String prom=excelParserService.getCellValueByAddress(row, headerSchema.getVersion());
                    String[]text=prom.split("\\.");
                    String version= "Вариант №"+
                           text[0];
                    UUID Version = excelParserService.getUUIDByValue( "r_version", version);
                    String itert=text[1];
                    if (Version != null||itert!=null) {
                        headerEntity.setVersion(Version);
                        headerEntity.setIteration(Integer.parseInt(itert.trim()));

                    } else {
                        throw new ExcelParserException("Не удалось определить версию и итерацию", headerSchema.getVersion());
                    }


                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getDateOfRelevance())) {
                   LocalDateTime date=excelParserService.getDateCellValue(row, headerSchema.getDateOfRelevance()).atTime(0,0);

                    headerEntity.setCreatedDate(date);
                }



            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }

        return headerEntity;
    }
    public List<ProjectEventEquipmentEntity> createProjectEventEntities(StreamingSheet sheet, HeaderEntity headerEntity) {
        List<ProjectEventEquipmentEntity> projectEventEquipmentEntities = new ArrayList<>();
        if (projectEventEquipmentSchema == null) {
            return projectEventEquipmentEntities;
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                if (row.getRowNum() + 1 >= projectEventEquipmentSchema.getStartRow()) {
                    UUID projectEvent = excelParserService
                            .getUUIDByCellAddress(row,"project_event", projectEventEquipmentSchema.getProjectEventColumn());
                    if (projectEvent  != null) {
                        ProjectEventEquipmentEntity projectEventEquipmentEntity = new ProjectEventEquipmentEntity();
                        projectEventEquipmentEntity.setProjectEventUuid(projectEvent);
                        projectEventEquipmentEntity.setUuid(UUID.randomUUID());
                        projectEventEquipmentEntity.setHeaderEconUuid(headerEntity.getUuid());
                        projectEventEquipmentEntity.setProjectEquipmentUuid(excelParserService
                                .getUUIDByCellAddress(row, "equipment", projectEventEquipmentSchema.getProjectEquipmentColumn()));

                        projectEventEquipmentEntity.setYear(parseStringToInt(excelParserService
                                .getCellValueByAddress(row, projectEventEquipmentSchema.getYearColumn())));
                        projectEventEquipmentEntity.setMeasureUnit(excelParserService
                                .getCellValueByAddress(row, projectEventEquipmentSchema.getMeasureUnitColumn()));
                        projectEventEquipmentEntity.setPhysicalVolume(excelParserService
                                .getBigDecimalValue(row, projectEventEquipmentSchema.getPhysicalVolumeColumn()));

                        projectEventEquipmentEntity.setEstimatedCost(excelParserService
                                .getBigDecimalValue(row, projectEventEquipmentSchema.getEstimatedCostColumn()));


                        projectEventEquipmentEntity.setFunctionalGroup(excelParserService
                                .getCellValueByAddress(row, projectEventEquipmentSchema.getFunctionalGroupColumn()));



                        projectEventEquipmentEntities.add(projectEventEquipmentEntity);
                    }
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return projectEventEquipmentEntities;
    }
}
