package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.dto.TimeType;
import ru.comita.lib.exception.ExcelParserException;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;
import ru.comita.nifi.dto.entity.OptionEntity;
import ru.comita.nifi.dto.entity.VolSummaryEntity;
import ru.comita.nifi.dto.schema.AnalyticsSchema;
import ru.comita.nifi.dto.schema.HeaderSchema;
import ru.comita.nifi.dto.schema.OptionsSchema;
import ru.comita.nifi.dto.schema.VolSummarySchema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.comita.lib.dto.TimeType.day;
import static ru.comita.lib.util.FsdParserUtil.fromDateString;
import static ru.comita.lib.util.FsdParserUtil.getTimeType;
import static ru.comita.lib.util.FsdParserUtil.parseStringToInt;

public class FSD2ParserService extends FSDParserService {

    private HeaderSchema headerSchema;
    private VolSummarySchema volSummarySchema;
    private OptionsSchema optionsSchema;

    public FSD2ParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.headerSchema = initSchema(schemaMap.get(SchemaName.HEADER), form, new TypeReference<>() {
        });
        this.volSummarySchema = initSchema(schemaMap.get(SchemaName.VOL_SUMMARY), form, new TypeReference<>() {
        });
        this.optionsSchema = initSchema(schemaMap.get(SchemaName.OPTIONS), form, new TypeReference<>() {
        });
    }

    public HeaderEntity createHeaderEntity(StreamingSheet sheet, String fileName) {
        HeaderEntity headerEntity = new HeaderEntity();
        headerEntity.setUuid(UUID.randomUUID());
        headerEntity.setCreatedDate(LocalDateTime.now());
        headerEntity.setUpdatedDate(LocalDateTime.now());
        headerEntity.setName(fileName);
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if ((row.getRowNum() + 1) > headerSchema.getLastRow()) {
                break;
            }
            try {
                if (FsdParserUtil.isInSameRow(row, headerSchema.getScenario())) {
                    headerEntity.setScenario(excelParserService
                            .getUUIDByCellAddress(row, "r_scenario", headerSchema.getScenario()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getHorizon())) {
                    String horizon = excelParserService.getCellValueByAddress(row, headerSchema.getHorizon());
                    if (horizon != null) {
                        headerEntity.setHorizon(excelParserService.getUUIDByValue("horizon", horizon));
                    }
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getPool())) {
                    String pool = excelParserService.getCellValueByAddress(row, headerSchema.getPool());
                    if (pool != null) {
                        pool = pool.replace("...", ".").replace("..", ".");
                        headerEntity.setPoolUuid(excelParserService.getUUIDByValue("pool", pool));
                    }
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getFacility())) {
                    String facility = excelParserService.getCellValueByAddress(row, headerSchema.getFacility());
                    if (facility != null) {
                        facility = facility.replace("...", ".").replace("..", ".");
                        headerEntity.setFacilityUuid(excelParserService.getUUIDByValue("facility", facility));
                    }
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getDevelopmentMethod())) {
                    headerEntity.setDevelopmentMethodUuid(excelParserService
                            .getUUIDByCellAddress(row, "r_development_method", headerSchema.getDevelopmentMethod()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getBusinessAssociate())) {
                    headerEntity.setBaUuid(excelParserService
                            .getUUIDByCellAddress(row, "business_associate", "long_name",
                                    headerSchema.getBusinessAssociate()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getModelDate())) {
                    LocalDate modelDate = excelParserService.getDateCellValue(row, headerSchema.getModelDate());
                    if (modelDate == null) {
                        throw new ExcelParserException("Не заполнена дата актуальности модели", headerSchema.getModelDate());
                    }
                    headerEntity.setYear(modelDate.getYear());
                    headerEntity.setModelDate(modelDate.atStartOfDay());
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getVersion())) {
                    headerEntity.setVersionUuid(excelParserService
                            .getUUIDByCellAddress(row, "r_version", headerSchema.getVersion()));
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return headerEntity;
    }

    public List<VolSummaryEntity> createVolSummaryEntities(StreamingSheet sheet, HeaderEntity header) {
        List<VolSummaryEntity> volSummaryEntities = new ArrayList<>();
        if (volSummarySchema == null) {
            return volSummaryEntities;
        }
        AnalyticsSchema analyticsSchema = volSummarySchema.getAnalytics();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() + 1 >= volSummarySchema.getStartRow()) {
                try {
                    volSummaryEntities.addAll(addVolSummaryEntitiesFromAnalyticsSchema(analyticsSchema, header, row));
                } catch (GroupedException e) {
                    getErrorLog().add(e.getMessage());
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return volSummaryEntities;
    }

    private List<VolSummaryEntity> addVolSummaryEntitiesFromAnalyticsSchema(AnalyticsSchema analyticsSchema,
                                                                            HeaderEntity header,
                                                                            Row row) {
        List<VolSummaryEntity> volSummaryEntities = new ArrayList<>();
        int colIndex = CellReference.convertColStringToIndex(analyticsSchema.getStartColumn());
        for (String analyticsName : analyticsSchema.getValues()) {
            VolSummaryEntity volSummaryFromAnalytics = createVolSummaryFromAnalytics(header, row, colIndex, analyticsName);
            if (volSummaryFromAnalytics != null) {
                volSummaryEntities.add(volSummaryFromAnalytics);
            }
            colIndex++;
        }
        return volSummaryEntities;
      }

    private VolSummaryEntity createVolSummaryFromAnalytics(HeaderEntity header,
                                                           Row row,
                                                           Integer analyticsColumnNum,
                                                           String analyticsName) {
        BigDecimal value = excelParserService.getBigDecimalValue(row, analyticsColumnNum);
        if (value == null) {
            return null;
        }

        Integer year;
        year = parseStringToInt(excelParserService
                .getCellValueByAddress(row, volSummarySchema.getYearColumn()));
        if (year == null) {
            return null;
        }
        String period = excelParserService
                .getCellValueByAddress(row, volSummarySchema.getPeriodColumn());
        TimeType timeType = getTimeType(period);
        LocalDateTime time = FsdParserUtil.getTime(year, timeType, period);

        if (time == null) {
            return null;
        }

        VolSummaryEntity volSummary = new VolSummaryEntity();
        volSummary.setHeaderUuid(header.getUuid());
        volSummary.setPeriodTypeUuid(excelParserService.getTimeTypeUUID(timeType));
        volSummary.setStartDate(time);
        volSummary.setAnalyticsUuid(excelParserService.getUUIDByValue("r_analytic", analyticsName));
        volSummary.setFacilityUuid(header.getFacilityUuid());
        volSummary.setPoolUuid(header.getPoolUuid());
        volSummary.setHorizonUuid(header.getHorizon());

        volSummary.setValue(value);
        return volSummary;
    }

    public List<VolSummaryEntity> createVolSummaryFromAdditionalAnalytics(StreamingSheet sheet,
                                                                          HeaderEntity header) {
        List<VolSummaryEntity> volSummaryEntities = new ArrayList<>();
        Map<String, String> additionalAnalytics = volSummarySchema.getAdditionalAnalytics();
        if (additionalAnalytics == null) {
            return volSummaryEntities;
        }
        String processedAnalytic = null;
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            if (additionalAnalytics.isEmpty()) {
                break;
            }
            Row row = rowIterator.next();
            for (String analyticName : additionalAnalytics.keySet()) {
                String analyticAddress = additionalAnalytics.get(analyticName);
                if (FsdParserUtil.isInSameRow(row, analyticAddress)) {
                    String stringValue = excelParserService.getCellValueByAddress(row, analyticAddress);
                    BigDecimal value;
                    try {
                        value = FsdParserUtil.getBigDecimalValueFromString(stringValue);
                    } catch (NumberFormatException e) {
                        throw new ExcelParserException(e.getLocalizedMessage(), analyticAddress);
                    }

                    if (value != null) {
                        VolSummaryEntity volSummary = new VolSummaryEntity();
                        volSummary.setHeaderUuid(header.getUuid());
                        volSummary.setAnalyticsUuid(excelParserService.getUUIDByValue("r_analytic", analyticName));
                        volSummary.setPoolUuid(header.getPoolUuid());
                        volSummary.setFacilityUuid(header.getFacilityUuid());
                        volSummary.setPeriodTypeUuid(excelParserService.getTimeTypeUUID(day));
                        volSummary.setStartDate(header.getModelDate());
                        volSummary.setValue(value);
                        processedAnalytic = analyticName;
                        volSummaryEntities.add(volSummary);
                    }
                }
            }
            if (processedAnalytic != null) {
                additionalAnalytics.remove(processedAnalytic);
            }
        }

        return volSummaryEntities;
    }

    public List<OptionEntity> createOptionEntities(StreamingSheet sheet, HeaderEntity headerEntity) {
        List<OptionEntity> optionEntities = new ArrayList<>();
        if (optionsSchema == null) {
            return optionEntities;
        }
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() + 1 >= optionsSchema.getStartRow()) {
                String projectStep = excelParserService
                        .getCellValueByAddress(row, optionsSchema.getProjectStepColumn());
                Integer year;

                if (projectStep != null) {
                    year = parseStringToInt(excelParserService
                            .getCellValueByAddress(row, optionsSchema.getStartDateColumn()));
                    if (year == null) {
                        return null;
                    }
                    String period = excelParserService
                            .getCellValueByAddress(row, optionsSchema.getPeriodType());
                    TimeType timeType = getTimeType(period);
                    LocalDateTime time = FsdParserUtil.getTime(year, timeType, period);

                    if (time == null) {
                        return null;
                    }
                    String equipment =  excelParserService
                            .getCellValueByAddress(row, optionsSchema.getProjectEquipmentColomn());
                    OptionEntity optionEntity = new OptionEntity();
                    optionEntity.setHeaderUuid(headerEntity.getUuid());
                    optionEntity.setProjectStepUuid(excelParserService.getUUIDByValue("project_step", projectStep));
                    optionEntity.setProjectEquipment(excelParserService.getUUIDByValue("equipment", equipment));
                    optionEntity.setPeriodTypeUuid(excelParserService.getTimeTypeUUID(timeType));
                    optionEntity.setStartDate(time);
                    // добавление парсинга атрибута volume типа BigDecimal
                    optionEntity.setVolume(excelParserService.getBigDecimalValue(row,CellReference.convertColStringToIndex(optionsSchema.getProjectVolumeColumn())));
                    optionEntity.setEdIzmer(excelParserService.getCellValueByAddress(row, optionsSchema.getEdIzmerColumn()));
                    optionEntity.setComent(excelParserService.getCellValueByAddress(row,optionsSchema.getComentColomn()));
                    optionEntities.add(optionEntity);
                }
            }

        }
        return optionEntities;
    }
}
