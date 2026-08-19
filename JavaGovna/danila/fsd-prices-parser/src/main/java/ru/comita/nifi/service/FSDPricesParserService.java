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
import ru.comita.nifi.dto.entity.PricesEntity;
import ru.comita.nifi.dto.schema.PricesSchema;
import ru.comita.nifi.dto.entity.HeaderEconomEntity;
import ru.comita.nifi.dto.schema.HeaderEconomSchema;
import static ru.comita.lib.util.FsdParserUtil.isInSameRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FSDPricesParserService extends FSDParserService {

    private PricesSchema pricesSchema;
    private HeaderEconomSchema headerEconom;

    public FSDPricesParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap)
            throws JsonProcessingException {
        this.pricesSchema = initSchema(schemaMap.get(SchemaName.PRICES), form, new TypeReference<>() {
        });
        this.headerEconom = initSchema(schemaMap.get(SchemaName.HEADER), form, new TypeReference<>() {
        });
    }

    public HeaderEconomEntity createHeaderEconomEntity(StreamingSheet sheet) {
        HeaderEconomEntity headerEconomEntity = new HeaderEconomEntity();
        headerEconomEntity.setUuid(UUID.randomUUID());
        headerEconomEntity.setCreatedDate(LocalDateTime.now());
        headerEconomEntity.setFsdSource(headerEconom.getFsdSource());
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {

                String baAdress = headerEconom.getBa();
                if (isInSameRow(row, baAdress)) {
                    headerEconomEntity.setBaUuid(
                            excelParserService.getUUIDByCellAddress(row, "business_associate", "long_name", baAdress));

                }
                String fieldAdress = headerEconom.getField();
                if (isInSameRow(row, fieldAdress)) {
                    headerEconomEntity.setFieldUuid(
                            excelParserService.getUUIDByCellAddress(row, "field", fieldAdress));
                }

                String headerYear = headerEconom.getYear();
                if (isInSameRow(row, headerYear)) {
                    Integer year = Integer.parseInt(
                            excelParserService.getCellValueByAddress(row, headerYear));
                    headerEconomEntity.setYear(year);
                }

                String headerScenario = headerEconom.getScenario();
                if (isInSameRow(row, headerScenario)) {
                    headerEconomEntity.setScenarioUuid(
                            excelParserService.getUUIDByCellAddress(row, "r_scenario", headerScenario));
                }
                if (row.getRowNum() + 1 > headerEconom.getLastRow()) {
                    break;
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return headerEconomEntity;
    }

    public List<PricesEntity> createPricesEntities(StreamingSheet sheet, HeaderEconomEntity headerEconomEntity) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<PricesEntity> pricesEntities = new ArrayList<>();
        UUID headerUuid = headerEconomEntity.getUuid();
        if (pricesSchema == null) {
            return pricesEntities;
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                if ((row.getRowNum()) >= pricesSchema.getStartRow() - 1) {
                    PricesEntity pricesEntity = new PricesEntity();
                    pricesEntity.setUuid(UUID.randomUUID());
                    pricesEntity.setHeaderUuid(headerUuid);

                    String analytic = excelParserService
                            .getCellValueByAddress(row, pricesSchema.getAnalyticColumn());
                    String measureUnit = excelParserService
                            .getCellValueByAddress(row, pricesSchema.getMeasureColumn());

                    if (analytic != null) {

                        // Сцепливаем с аналитикой, если есть единица измерения
                        if (measureUnit != null) {
                            analytic = analytic + " " + measureUnit;
                        }

                        pricesEntity.setAnalyticUuid(excelParserService
                                .getUUIDByValue("r_analytic", analytic));

                    } else {
                        throw new ExcelParserException("Ошибка при парсинге параметра ",
                                pricesSchema.getAnalyticColumn());
                    }

                    BigDecimal value = excelParserService.getBigDecimalValue(row, pricesSchema.getValueColumn());
                    if (value != null) {
                        pricesEntity.setValue(value);
                        pricesEntities.add(pricesEntity);
                    } else {
                        componentLog
                                .warn("Не заполнено значение аналитики в строке " + (row.getRowNum() + 1) + analytic);
                        continue;
                    }
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return pricesEntities;
    }

}
