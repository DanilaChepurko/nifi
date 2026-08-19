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
import ru.comita.nifi.dto.entity.FieldEntity;
import ru.comita.nifi.dto.schema.FieldSchema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static ru.comita.lib.util.FsdParserUtil.parseStringToInt;

public class FSDFieldParserService extends FSDParserService {

    private FieldSchema fieldSchema;

    public FSDFieldParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.fieldSchema = initSchema(schemaMap.get(SchemaName.FIELD), form, new TypeReference<>() {
        });
    }

    public List<FieldEntity> createFieldEntities(StreamingSheet sheet) {
        List<FieldEntity> fieldEntities = new ArrayList<>();
        if (fieldSchema == null) {
            return fieldEntities;
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        UUID baUuid = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                if (FsdParserUtil.isInSameRow(row, fieldSchema.getBusinessAssociate())) {
                    baUuid = excelParserService
                            .getUUIDByCellAddress(row, "business_associate", "long_name",
                                    fieldSchema.getBusinessAssociate());
                }
                if ((row.getRowNum() + 1) >= fieldSchema.getStartRow()) {
                    String name = excelParserService
                            .getCellValueByAddress(row, fieldSchema.getNameColumn());
                    if (name != null) {
                        FieldEntity fieldEntity = new FieldEntity();
                        fieldEntity.setName(name);
                        fieldEntity.setBaUuid(baUuid);
                        fieldEntity.setAreaUuid(excelParserService
                                .getUUIDByCellAddress(row, "area", fieldSchema.getAreaColumn()));
                        fieldEntity.setTypeUuid(excelParserService
                                .getUUIDByCellAddress(row, "r_field_type", fieldSchema.getTypeColumn()));
                        fieldEntity.setLicenses(excelParserService
                                .getCellValueByAddress(row, fieldSchema.getLicenseColumn()));
                        fieldEntity.setEsg("да".equalsIgnoreCase(excelParserService
                                .getCellValueByAddress(row, fieldSchema.getEsgColumn())));
                        fieldEntity.setYearOpen(parseStringToInt(excelParserService
                                .getCellValueByAddress(row, fieldSchema.getYearOpenColumn())));//добавление нового параметра
                        fieldEntity.setProductSaleUuid(excelParserService
                                .getUUIDByCellAddress(row, "product_sale", fieldSchema.getProducSaleColumn()));

                        fieldEntity.setUuid(UUID.randomUUID());
                        fieldEntities.add(fieldEntity);
                    }
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return fieldEntities;
    }
}
