package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.ExcelParserException;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.exception.SchemaException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;

import ru.comita.nifi.dto.entity.ParamEntity;
import ru.comita.nifi.dto.entity.OpfEntity;

import ru.comita.nifi.dto.schema.AnalyticsSchema;
import ru.comita.nifi.dto.schema.HeaderSchema;
import ru.comita.nifi.dto.schema.ParamSchema;
import ru.comita.nifi.dto.schema.StranSchema;
import ru.comita.nifi.dto.schema.OpfSchema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FSDOperatingCostParserService extends FSDParserService {
    private HeaderSchema headerSchema;
    private ParamSchema paramSchema;
    private StranSchema stranSchema;
    private OpfSchema opfSchema;
    private boolean nullcreate;

    public FSDOperatingCostParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.headerSchema = initSchema(schemaMap.get(SchemaName.HEADER), form, new TypeReference<>() {
        });
        this.paramSchema = initSchema(schemaMap.get(SchemaName.HORIZON_PARAM), form, new TypeReference<>() {
        });
        this.opfSchema=initSchema(schemaMap.get(SchemaName.OPF), form, new TypeReference<>() {
        });
    }

    public HeaderEntity createHeaderEntity(StreamingSheet sheet, String fileName) {
        HeaderEntity headerEntity = new HeaderEntity();
        headerEntity.setUuid(UUID.randomUUID());
        headerEntity.setName(fileName);
        headerEntity.setCreatedDate(LocalDateTime.now());
        Iterator<Row> rowIterator = sheet.rowIterator();
        String field="";
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
                    UUID BaUuid = excelParserService.getUUIDByCellAddress(row, "business_associate",
                            "long_name", headerSchema.getBusinessAssociate());

                    if (BaUuid != null) {
                        headerEntity.setBaUuid(BaUuid);
                    } else {

                        throw new ExcelParserException("Не удалось определить ГДО", headerSchema.getBusinessAssociate());
                    }
                }

                if (FsdParserUtil.isInSameRow(row, headerSchema.getField())) {
                    field=excelParserService.getCellValueByAddress(row, headerSchema.getField());
                    UUID Field = excelParserService.getUUIDByValue( "field", field);;

                    if (Field != null) {
                        headerEntity.setFieldUuid(Field);
                    } else {
                        throw new ExcelParserException("Не удалось определить месторождение", headerSchema.getField());
                    }
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getHorizon())) {
                    System.out.println(" Вошол в горизонт "+field);
                    String horizon= field+"."
                            +excelParserService.getCellValueByAddress(row, headerSchema.getHorizon());
                    UUID Horizon = excelParserService.getUUIDByValue( "horizon", horizon);

                    if (Horizon != null) {
                        headerEntity.setHorizonUuid(Horizon);
                    } else {
                        throw new ExcelParserException("Не удалось определить горизонт", headerSchema.getHorizon());
                    }
                }
                headerEntity.setFsdSource(headerSchema.getFsdSource());


            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }

        return headerEntity;
    }

    public List<ParamEntity> createParamEntities(StreamingSheet sheet, HeaderEntity header, int schema) {
        List<ParamEntity> paramEntities = new ArrayList<>();
        if (paramSchema == null) {
            throw new SchemaException("Не удалось определить схему для paramSchema");
        }

        if (schema == 1) {
            stranSchema = paramSchema.getStr1();
            nullcreate = true;
        }
        if (schema == 3) {
            stranSchema = paramSchema.getStr3();
            nullcreate = false;
        }
        if (schema == 4) {
            stranSchema = paramSchema.getStr4();
            nullcreate = false;
        }
        if (stranSchema != null) {

            Iterator<Row> rowIterator = sheet.rowIterator();
            AnalyticsSchema analyticsSchema = stranSchema.getValue();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() + 1 >= stranSchema.getStartRow()) {
                    try {
                        paramEntities.addAll(
                                addParamEntitieFromStrSchema(stranSchema, analyticsSchema, header,
                                        row));
                    } catch (GroupedException e) {
                        getErrorLog().add(e.getMessage());
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        return paramEntities;

    }


    public List<ParamEntity> addParamEntitieFromStrSchema(StranSchema stranSchema, AnalyticsSchema analyticsSchema, HeaderEntity header,
                                                          Row row) {
        List<ParamEntity> paramEntities = new ArrayList<>();
        int colIndex = CellReference.convertColStringToIndex(analyticsSchema.getStartColumn());

        for (String analyticsName : analyticsSchema.getTypeName()) {
            ParamEntity materialBalFromAnalytics = addEntitis(
                    header, row, colIndex, analyticsName);
            if (materialBalFromAnalytics != null) {
                paramEntities.add(materialBalFromAnalytics);
            }
            colIndex++;
        }

        return paramEntities;
    }

    public ParamEntity addEntitis(HeaderEntity header, Row row, Integer analyticsColumnNum, String analyticsName) {
        String znach=excelParserService.getCellValueByAddress(row,analyticsColumnNum);
        if (znach!="ERROR:  #DIV/0!"){
        BigDecimal value = excelParserService.getBigDecimalValue(row, analyticsColumnNum);
        ParamEntity paramEntity=null;
        if (value!=null ){
        if (nullcreate) {
            if (value.doubleValue() == 0) {
                return null;
            }
            paramEntity = new ParamEntity();
            paramEntity.setHeaderUuid(header.getUuid());
            paramEntity.setCreatedDate(LocalDateTime.now());
            paramEntity.setValue(value);
            String cost=excelParserService.getCellValueByAddress(row, stranSchema.getUnitColumn());
            String analytic;
            if (cost!=null) {
                 analytic = excelParserService.getCellValueByAddress(row, stranSchema.getAnalyticsColumn()) + " " + cost;
            }else {
                 analytic = excelParserService.getCellValueByAddress(row, stranSchema.getAnalyticsColumn());}

            if (analytic!=null){
            paramEntity.setAnalyticsUuid(excelParserService.getUUIDByValue("r_analytic", analytic));
            paramEntity.setFuid(analyticsName);}
            else {return null;}
        } else
        {
            if (value == null) {
                return null;
            }
            paramEntity = new ParamEntity();
            paramEntity.setHeaderUuid(header.getUuid());
            paramEntity.setCreatedDate(LocalDateTime.now());
            paramEntity.setValue(value);
            String cost=excelParserService.getCellValueByAddress(row, stranSchema.getUnitColumn());
            String analytic;
            if (cost!=null) {
                 analytic = excelParserService.getCellValueByAddress(row, stranSchema.getAnalyticsColumn()) + " " + cost;
            }else {
                 analytic = excelParserService.getCellValueByAddress(row, stranSchema.getAnalyticsColumn());}

            if (analytic!=null){
                paramEntity.setAnalyticsUuid(excelParserService.getUUIDByValue("r_analytic", analytic));
                paramEntity.setFuid(analyticsName);}
            else {return null;}
        }}
        return paramEntity;
    } return null;}


    public List<OpfEntity> createOpfEntities(StreamingSheet sheet, HeaderEntity header) {
        List<OpfEntity> opfEntities = new ArrayList<>();
        if (opfSchema == null) {
            throw new SchemaException("Не удалось определить схему для opf");
        }
        AnalyticsSchema analyticsSchema = opfSchema.getValue();
        if (analyticsSchema != null) {
            Iterator<Row> rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() + 1 >= opfSchema.getStartRow()) {
                    try {
                        opfEntities.addAll(
                                addOpfEntitiesFromAnalyticsSchema(analyticsSchema, header,
                                        row));
                    } catch (GroupedException e) {
                        getErrorLog().add(e.getMessage());
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }

        return opfEntities;
    }

    private List<OpfEntity> addOpfEntitiesFromAnalyticsSchema(AnalyticsSchema analyticsSchema,
                                                                              HeaderEntity header,
                                                                              Row row) {
        List<OpfEntity> opfEntities = new ArrayList<>();
        int colIndex = CellReference.convertColStringToIndex(analyticsSchema.getStartColumn());
        for (String analyticsName : analyticsSchema.getTypeName()) {
            OpfEntity opfFromAnalytics = createOpfFromAnalytics(
                    header, row, colIndex, analyticsName);
            if (opfFromAnalytics != null) {
                opfEntities.add(opfFromAnalytics);
            }
            colIndex++;
        }
        return opfEntities;
    }

    private OpfEntity createOpfFromAnalytics(HeaderEntity header,
                                                             Row row,
                                                             Integer analyticsColumnNum,
                                                             String analyticsName) {
        BigDecimal value = excelParserService.getBigDecimalValue(row, analyticsColumnNum);
        if (value == null) {
            return null;
        }

        OpfEntity opfEntity = new OpfEntity();
        opfEntity.setHeaderUuid(header.getUuid());
        opfEntity.setCreatedDate(LocalDateTime.now());
        opfEntity.setAnalyticsUuid(excelParserService.getUUIDByValue("r_analytic", analyticsName));
        String name  = excelParserService.getCellValueByAddress(row, opfSchema.getNameColomn());
        if (name == null) {
            throw new ExcelParserException("Не указано название", opfSchema.getNameColomn(), row.getRowNum() + 1);
        }
        opfEntity.setName(name);

        opfEntity.setValue(value);
        return opfEntity;
    }



}
