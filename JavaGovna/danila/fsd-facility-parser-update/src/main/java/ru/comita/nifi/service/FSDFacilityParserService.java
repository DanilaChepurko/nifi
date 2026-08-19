package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.ExcelParserException;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.FacilityEntity;
import ru.comita.nifi.dto.schema.FacilitySchema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FSDFacilityParserService extends FSDParserService {

    private FacilitySchema facilitySchema;

    public FSDFacilityParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.facilitySchema = initSchema(schemaMap.get(SchemaName.FACILITY), form, new TypeReference<>() {
        });
    }

    public List<FacilityEntity> createFacilityEntities(StreamingSheet sheet) {
        List<FacilityEntity> facilityEntities = new ArrayList<>();
        if (facilitySchema == null) {
            return facilityEntities;
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        UUID fieldUUID = null;
        String fieldName = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                if (FsdParserUtil.isInSameRow(row, facilitySchema.getField())) {
                    fieldName = excelParserService.getCellValueByAddress(row, facilitySchema.getField());
                    fieldUUID = excelParserService.getUUIDByValue("field", fieldName);
                }
                if ((row.getRowNum() + 1) >= facilitySchema.getStartRow()) {
                    String name = excelParserService
                            .getCellValueByAddress(row, facilitySchema.getNameColumn());
                    if (name != null) {
                        if (fieldName == null) {
                            throw new ExcelParserException("Не заполнено месторождение", facilitySchema.getField());
                        }
                        FacilityEntity facilityEntity = new FacilityEntity();
                        facilityEntity.setFieldUuid(fieldUUID);
                        facilityEntity.setShortName(name);
                        String horizon = excelParserService
                                .getCellValueByAddress(row, facilitySchema.getHorizonColumn());
                        if (horizon != null) {
                            horizon = fieldName.concat(".").concat(horizon);
                            facilityEntity.setHorizonUuid(excelParserService
                                    .getUUIDByValue("horizon", horizon));
                        } else {
                            horizon = fieldName;
                        }
                        String horizonArea = excelParserService
                                .getCellValueByAddress(row, facilitySchema.getHorizonAreaColumn());
                        if (horizonArea != null) {
                            horizonArea = horizon.concat(".").concat(horizonArea);
                            facilityEntity.setHorizonAreaUuid(excelParserService
                                    .getUUIDByValue("horizon_area", horizonArea));
                        } else {
                            horizonArea = horizon;
                        }

                        name = horizonArea.concat(".").concat(name);
                        facilityEntity.setDkc(excelParserService
                                .getCellValueByAddress(row, facilitySchema.getDkcColumn()));
                        facilityEntity.setName(name);
                        facilityEntity.setUuid(UUID.randomUUID());
                        facilityEntities.add(facilityEntity);
                    }
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return facilityEntities;
    }
}
