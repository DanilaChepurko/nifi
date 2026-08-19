package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;

import lombok.val;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.dto.TimeType;
import ru.comita.lib.exception.ExcelParserException;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.exception.SchemaException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;
import ru.comita.nifi.dto.entity.VolSummaryEntity;
import ru.comita.nifi.dto.entity.HeaderMetaInfEntity;
import ru.comita.nifi.dto.schema.AnalyticsSchema;
import ru.comita.nifi.dto.schema.HeaderSchema;
import ru.comita.nifi.dto.schema.VolSummarySchema;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.comita.lib.util.FsdParserUtil.getTime;
import static ru.comita.lib.util.FsdParserUtil.getTimeType;
import static ru.comita.lib.util.FsdParserUtil.parseStringToInt;

public class FSD5ParserService extends FSDParserService {

    private HeaderSchema headerSchema;
    private VolSummarySchema volSummarySchema;

    public FSD5ParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.headerSchema = initSchema(schemaMap.get(SchemaName.HEADER), form, new TypeReference<>() {
        });
        this.volSummarySchema = initSchema(schemaMap.get(SchemaName.VOL_SUMMARY), form, new TypeReference<>() {
        });
    }
    protected class HeaderParseResult{
        protected final HeaderEntity headerEntity;
        protected final HeaderMetaInfEntity headerMetaInfEntity;
        
        protected HeaderParseResult(HeaderEntity headerEntity, HeaderMetaInfEntity headerMetaInfEntity){
            this.headerEntity = headerEntity;
            this.headerMetaInfEntity = headerMetaInfEntity;
        }
    }

    public HeaderParseResult createHeaderParseResult(StreamingSheet sheet, String fileName) {
        HeaderEntity headerEntity = new HeaderEntity();
        HeaderMetaInfEntity headerMetaInfEntity = new HeaderMetaInfEntity();

        UUID headerUuid = UUID.randomUUID();
        headerEntity.setUuid(headerUuid);
        headerMetaInfEntity.setHeaderUuid(headerUuid);
        headerEntity.setCreatedDate(LocalDateTime.now());
        headerEntity.setUpdatedDate(LocalDateTime.now());
        headerEntity.setName(fileName);
        headerEntity.setDopScenario(excelParserService.getUUIDByValue("r_scenario", headerSchema.getDopScenario()));
        
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() > (headerSchema.getLastRow()-1)) {
                break;
            }
            try {
                if (FsdParserUtil.isInSameRow(row, headerSchema.getScenario())) {
                    headerEntity.setScenario(excelParserService
                            .getUUIDByCellAddress(row, "r_scenario", headerSchema.getScenario()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getField())) {
                    headerEntity.setFieldUuid(excelParserService
                            .getUUIDByCellAddress(row, "field", headerSchema.getField()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getHorizon())) {
                    headerEntity.setHorizonUuid(excelParserService
                            .getUUIDByCellAddress(row, "horizon", headerSchema.getHorizon()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getYear())) {
                    headerEntity.setYear(parseStringToInt(excelParserService
                            .getCellValueByAddress(row, headerSchema.getYear())));
                    if (headerEntity.getYear() == null) {
                        throw new ExcelParserException("Не указан год", headerSchema.getYear());
                    }
                    headerEntity.setModelDate(LocalDate.of(headerEntity.getYear(), 1, 1).atStartOfDay());
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getVersion())) {
                    headerEntity.setVersionUuid(excelParserService
                            .getUUIDByCellAddress(row, "r_version", headerSchema.getVersion()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getBa())) {
                    headerEntity.setBaUuid(excelParserService.getUUIDByCellAddress(row, "business_associate", "long_name", headerSchema.getBa()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getFluidType())) {
                    headerEntity.setFluidType(excelParserService
                            .getCellValueByAddress(row,  headerSchema.getFluidType()));
                }

                if (FsdParserUtil.isInSameRow(row, headerSchema.getAuthor())) {
                    headerMetaInfEntity.setAuthor(excelParserService
                            .getCellValueByAddress(row, headerSchema.getAuthor()));
                }

                if (FsdParserUtil.isInSameRow(row, headerSchema.getFillInDate())) {
                    LocalDate stringDate = excelParserService.getDateCellValue(row, headerSchema.getFillInDate());
                    headerMetaInfEntity.setFillInDate(stringDate);
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getSyncScenario())) {
                    headerMetaInfEntity.setSyncScenario(excelParserService
                            .getUUIDByCellAddress(row, "r_scenario",headerSchema.getSyncScenario()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getSyncComparedScenario())) {
                    headerMetaInfEntity.setSyncComparedScenario(excelParserService
                            .getUUIDByCellAddress(row, "r_scenario",headerSchema.getSyncComparedScenario()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getSyncVersion())) {
                    headerMetaInfEntity.setSyncVersion(excelParserService
                            .getUUIDByCellAddress(row, "r_version", headerSchema.getSyncVersion()));
                }
                if (FsdParserUtil.isInSameRow(row, headerSchema.getSyncComparedVersion())) {
                    headerMetaInfEntity.setSyncComparedVersion(excelParserService
                            .getUUIDByCellAddress(row, "r_version", headerSchema.getSyncComparedVersion()));
                }

            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return new HeaderParseResult(headerEntity, headerMetaInfEntity);
    }

    public List<VolSummaryEntity> createVolSummaryEntities(StreamingSheet sheet, HeaderEntity header) {
        List<VolSummaryEntity> volSummaryEntities = new ArrayList<>();
        VolSummarySchema volSummarySchema;
        volSummarySchema = this.volSummarySchema;
        if (volSummarySchema == null) {
            return volSummaryEntities;
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if (row.getRowNum() + 1 >= volSummarySchema.getStartRow()) {
                boolean isIgnoredRow = isRowEmptyOrIgnored(row);
                if (isIgnoredRow) {
                    logger.warn("Пропущена незаполненная строка {}", row.getRowNum() + 1);
                    continue; 
                }

                try {
                    UUID projectStepUuid = excelParserService
                            .getUUIDByCellAddress(row,"project_step","code", volSummarySchema.getProjectStepColumn());
                    if (projectStepUuid == null){
                        throw new ExcelParserException("Не найдена стройка: не заполнен код стройки", volSummarySchema.getProjectStepColumn(), row.getRowNum() + 1);
                    }
                    UUID projectEventUuid = excelParserService.getUUIDByCellAddress(row, "project_event", "name", volSummarySchema.getProjectEventColumn());
                    if (projectEventUuid == null){
                        throw new ExcelParserException("Не заполнено кракткое наименование мероприятия", volSummarySchema.getProjectEventColumn(), row.getRowNum() + 1);
                    }
                    volSummaryEntities.addAll(
                            addVolSummaryEntitiesFromAnalyticsSchema(volSummarySchema, header,
                                    row, projectStepUuid, projectEventUuid));
                } catch (GroupedException e) {
                    getErrorLog().add(e.getMessage());
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return volSummaryEntities;
    }

    private List<VolSummaryEntity> addVolSummaryEntitiesFromAnalyticsSchema(VolSummarySchema volSummarySchema,
                                                                            HeaderEntity header,
                                                                            Row row,
                                                                            UUID projectStepUuid,
                                                                        UUID projectEventUuid) {
        List<VolSummaryEntity> volSummaryEntities = new ArrayList<>();
        AnalyticsSchema analyticsSchema = volSummarySchema.getAnalytics();
        if (analyticsSchema == null) {
            throw new SchemaException("Отсутсвует схема аналитики");
        }
        List<String> analytics = analyticsSchema.getValues();
        List<SimpleEntry<Integer,String>> filteredWithIndex =  getFilteredAnalyticsWithColumnIndeces(analytics, header);
        // Фильтруем аналитики, чтобы не добавлять строки с null значениями value
        int startColIndex = CellReference.convertColStringToIndex(analyticsSchema.getStartColumn());
        for (SimpleEntry<Integer,String> entry : filteredWithIndex) {
            int colIndex = startColIndex + entry.getKey();
            String analyticsName = entry.getValue();
            
            VolSummaryEntity volSummaryFromAnalytics = createVolSummaryFromAnalytics(
                volSummarySchema, header, row, colIndex, analyticsName, projectStepUuid, projectEventUuid);
            if (volSummaryFromAnalytics != null) {
                volSummaryEntities.add(volSummaryFromAnalytics);
            }
        }
        return volSummaryEntities;
    }

    private VolSummaryEntity createVolSummaryFromAnalytics(VolSummarySchema volSummarySchema,
                                                           HeaderEntity header,
                                                           Row row,
                                                           Integer analyticsColumnNum,
                                                           String analyticsName,
                                                           UUID projectStepUuid,
                                                        UUID projectEventUuid) {
        BigDecimal value = excelParserService.getBigDecimalValue(row, analyticsColumnNum);
        if (value == null) {
            return null;
        }

        Integer year = parseStringToInt(excelParserService
                .getCellValueByAddress(row, volSummarySchema.getYearColumn()));

        if (year == null) {
            year = header.getYear();
        }
        
        TimeType timeType = getTimeType(excelParserService
                .getCellValueByAddress(row, volSummarySchema.getPeriodTypeColumn()));
        LocalDateTime time = FsdParserUtil.getTime(year, timeType, excelParserService
                .getCellValueByAddress(row, volSummarySchema.getPeriodTypeColumn()));
        if (time == null) {
            time = getTime(year, timeType, null);
        }

        VolSummaryEntity volSummary = new VolSummaryEntity();
        volSummary.setHeaderUuid(header.getUuid());
        volSummary.setPeriodTypeUuid(excelParserService.getTimeTypeUUID(timeType));
        volSummary.setStartDate(time);
        volSummary.setYear(
            tryParseInt(
                excelParserService.getCellValueByAddress(
                    row, volSummarySchema.getYearColumn())
                )
            );
        volSummary.setAnalyticsUuid(excelParserService.getUUIDByValue("r_analytic", analyticsName));
        volSummary.setProjectStepUuid(projectStepUuid);
        volSummary.setProjectEventUuid(projectEventUuid);
        volSummary.setEquipmentCategory(excelParserService.getCellValueByAddress(
                    row, volSummarySchema.getEquipmentCategoryColumn()
                ));
        volSummary.setProjectStepGroup(tryParseInt(excelParserService
                .getCellValueByAddress(row, volSummarySchema.getProjectStepGroupColumn())));
        volSummary.setValue(value);
        return volSummary;
    }

    private List<SimpleEntry<Integer,String>> getFilteredAnalyticsWithColumnIndeces(List<String> analytics, HeaderEntity header){
        String fluidType = header.getFluidType();

        // Выбираем функцию для фильтра в на основании типа флюида из хедера
        Predicate<String> matchesFluidType; 
        if ("Нефть".equals(fluidType)) {
        matchesFluidType = s -> s.toLowerCase().contains("пнг")|| s.toLowerCase().contains("нефт");
        } else if("Газ".equals(fluidType)){
            matchesFluidType = s->s.toLowerCase().contains("газ");
        } 
        else if ("Газ+Конденсат".equals(fluidType)) {
            matchesFluidType = s -> s.toLowerCase().contains("газ") || s.toLowerCase().contains("конденсат");
        } else {
            matchesFluidType = s -> s.toLowerCase().contains(s);
        }
        
        List<AbstractMap.SimpleEntry<Integer, String>> filteredWithIndex = IntStream.range(0, analytics.size())
        .mapToObj(i -> new SimpleEntry<>(i, analytics.get(i)))
        .filter(entry -> matchesFluidType.test(entry.getValue()))
        .collect(Collectors.toList());
        return filteredWithIndex;
    }

    private boolean isRowEmptyOrIgnored(Row row) {
        if (row == null) {
            return true;
        }
        int lastCellNum = row.getLastCellNum();
        if (lastCellNum <= 0) {
            return true;
        }

        return IntStream.range(0, lastCellNum)
                .mapToObj(row::getCell)
                .allMatch(cell -> isCellEmptyOrIgnored(cell));
    }

    private boolean isCellEmptyOrIgnored(Cell cell) {
        if (cell == null) {
            return true;
        }   

    CellType cellType = cell.getCellType();
    if (cellType == CellType.BLANK) {
        return true;
    }
        switch (cellType) {
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == 0.0) {
                    return true;
                } else {
                    // Если не 0, - считаем, что ячейка НЕ пуста
                    return false;
                }
            // Формулу отдельно не обрабатываем -- просто берем ее строковое значение и смотрим на него
            case ERROR:
                return true;
            case FORMULA:
            case STRING:
                String stringValue = cell.getStringCellValue().toLowerCase().trim();
                return stringValue.startsWith("необходимо") || stringValue.isEmpty() || stringValue.equals("0") || stringValue.contains("#");

            default:
                return false;
        }
    }

    private Integer tryParseInt(String value) {
        if (value == null)
            return null;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return (int) Double.parseDouble(value);
        }
    }

}
