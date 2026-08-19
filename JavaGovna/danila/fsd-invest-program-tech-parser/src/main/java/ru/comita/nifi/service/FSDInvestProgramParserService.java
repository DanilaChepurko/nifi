package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.exception.ExcelParserException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.InvestProgramEntity;
import ru.comita.nifi.dto.schema.InvestProgramSchema;

import static ru.comita.lib.util.FsdParserUtil.getTimeType;
import static ru.comita.lib.util.FsdParserUtil.parseStringToInt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FSDInvestProgramParserService extends FSDParserService {
    private InvestProgramSchema investProgramSchema;

    public FSDInvestProgramParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.investProgramSchema = initSchema(schemaMap.get(SchemaName.HEADER), form, new TypeReference<>() {
        });
    }

    public List<InvestProgramEntity> createInvestProgramEntityes(StreamingSheet sheet) {
        List<InvestProgramEntity> InvestProgramEntityes = new ArrayList<>();
        if (investProgramSchema == null) {
            return InvestProgramEntityes;
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        String sourceIps = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                if (FsdParserUtil.isInSameRow(row, investProgramSchema.getSourceIp())) {

                    sourceIps = excelParserService
                            .getCellValueByAddress(row,
                                    investProgramSchema.getSourceIp());

                }
                if (row.getRowNum() + 1 >= investProgramSchema.getStartRow()) {

                    String constructionNamber = excelParserService
                            .getCellValueByAddress(row, investProgramSchema.getConstructionNamberColumn());
                    if (constructionNamber != null) {
                        InvestProgramEntity investProgramEntity = new InvestProgramEntity();
                    String field = excelParserService
                            .getCellValueByAddress(row, investProgramSchema.getFieldColumn());
                    if (field != null) {

                        investProgramEntity.setField(excelParserService
                                .getUUIDByValue("field", field));
                        String horizon = excelParserService
                                .getCellValueByAddress(row, investProgramSchema.getHorizonColumn());
                        if (horizon != null) {
                            horizon = field + "." + excelParserService
                                    .getCellValueByAddress(row, investProgramSchema.getHorizonColumn());
                            investProgramEntity.setHorizon(excelParserService
                                    .getUUIDByValue("horizon", horizon));
                        } else {
                            horizon = field;
                        }
                        String horizonArea = excelParserService
                                .getCellValueByAddress(row, investProgramSchema.getHorizonAreaColumn());
                        if (horizonArea != null) {
                            horizonArea = horizon + "." + excelParserService
                                    .getCellValueByAddress(row, investProgramSchema.getHorizonAreaColumn());
                            investProgramEntity.setHorizonArea(excelParserService
                                    .getUUIDByValue("horizon_area", horizonArea));
                        }

                    }



                        investProgramEntity.setConstructionNamber(parseStringToInt(constructionNamber));
                        investProgramEntity.setUuid(UUID.randomUUID());

                        investProgramEntity.setConstructionCod(excelParserService
                                .getCellValueByAddress(row, investProgramSchema.getConstructionCodColumn()));
                        investProgramEntity.setSourceIp(sourceIps);
                        investProgramEntity.setEquipmentName(excelParserService
                                .getCellValueByAddress(row, investProgramSchema.getEquipmentNameColumn()));
                        investProgramEntity.setVolumeEquipment(excelParserService
                                .getBigDecimalValue(row, investProgramSchema.getVolumeEquipmentColumn()));

                        investProgramEntity.setVolumeUnit(excelParserService
                                .getCellValueByAddress(row, investProgramSchema.getVolumeUnitColumn()));

                        String quarter = excelParserService.getCellValueByAddress(row, investProgramSchema.getStartQuarterColumn());
                        if (quarter == null || quarter.isEmpty()) {
                            quarter = "4";
                        }
                        if (quarter.contains(".")) {
                            quarter = quarter.substring(0, quarter.indexOf("."));
                        }

                        quarter = quarter + " кв.";


                        Integer year= parseStringToInt(excelParserService
                                .getCellValueByAddress(row, investProgramSchema.getStartYearAssociate()));
                        if(year==null){
                            throw new ExcelParserException("Нету года ввода", investProgramSchema.getStartYearAssociate());

                        }
                        LocalDateTime date =FsdParserUtil.getTime(year,getTimeType(quarter),quarter);
                        investProgramEntity.setStartDate(date);

                        InvestProgramEntityes.add(investProgramEntity);
                    }
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return InvestProgramEntityes;
    }
}
