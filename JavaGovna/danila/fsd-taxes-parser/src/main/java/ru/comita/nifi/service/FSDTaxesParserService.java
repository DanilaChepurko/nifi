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
import ru.comita.nifi.dto.entity.TaxesEntity;
import ru.comita.nifi.dto.schema.TaxesSchema;
import ru.comita.nifi.dto.entity.HeaderEconomEntity;
import ru.comita.nifi.dto.schema.HeaderEconomSchema;
import ru.comita.lib.util.FsdParserUtil;
import static ru.comita.lib.util.FsdParserUtil.isInSameRow;
import static ru.comita.lib.util.FsdParserUtil.parseStringToInt;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FSDTaxesParserService extends FSDParserService {

    private TaxesSchema taxesSchema;
    private HeaderEconomSchema headerEconom;

    public FSDTaxesParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap)
            throws JsonProcessingException {
        this.taxesSchema = initSchema(schemaMap.get(SchemaName.TAXES), form, new TypeReference<>() {
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
                if (isInSameRow(row, headerEconom.getYear())) {
                    Integer year = parseStringToInt(
                            excelParserService.getCellValueByAddress(row, headerEconom.getYear()));
                    headerEconomEntity.setYear(year);
                }
                if (isInSameRow(row, headerEconom.getScenario())) {
                    headerEconomEntity.setScenarioUuid(
                            excelParserService.getUUIDByCellAddress(row, "r_scenario", headerEconom.getScenario()));
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

    public List<TaxesEntity> createTaxesEntities(StreamingSheet sheet, HeaderEconomEntity headerEconomEntity, boolean flag) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<TaxesEntity> taxesEntities = new ArrayList<>();
        UUID headerUuid = headerEconomEntity.getUuid();
        if (taxesSchema == null) {
            return taxesEntities;
        }

        // Отслеживаем состояние неявных мильтииндексов
        int currentHeaderIndex = -1;
        List<String> headerNames = new ArrayList<>();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {

                if(flag)
                {
                  if ((row.getRowNum()) >= taxesSchema.getStartRow() - 1) {
                      TaxesEntity taxesEntity = new TaxesEntity();
                      taxesEntity.setUuid(UUID.randomUUID());
                      taxesEntity.setHeaderUuid(headerUuid);

                      String analytic = excelParserService
                              .getCellValueByAddress(row, taxesSchema.getAnalyticColumn());
                      String measureUnit = excelParserService
                              .getCellValueByAddress(row, taxesSchema.getMeasureColumn());

                      if (analytic != null) {

                          // При обнаружении текущей строки в списке header`ов, инициализируем текущий
                          // индекс и добавляем в список имен, переходим к следующей строке
                          if (isRowMultiindex(row)) {
                              headerNames.add(analytic);
                              currentHeaderIndex++;
                              continue;
                          }

                          // Если мы внутри мультииндекса, то обновляем имя analytic, сцепливая с головным
                          // индексом
                          if (currentHeaderIndex >= 0 && isInsideMultiindex(row, currentHeaderIndex)) {
                              analytic.trim();
                              analytic = headerNames.get(currentHeaderIndex) + " " + analytic;
                          }

                          // Сцепливаем с аналитикой, если есть единица измерения
                          if (measureUnit != null) {
                              analytic = analytic + " " + measureUnit;
                          }

                          taxesEntity.setAnalyticUuid(excelParserService
                                  .getUUIDByValue("r_analytic", analytic));

                      } else {
                          if (FsdParserUtil.isInSameRow(row, taxesSchema.getValueColumn())) {


                              System.out.println(" Строка " + row);

                              throw new ExcelParserException("Ошибка при парсинге параметра ",
                                      taxesSchema.getAnalyticColumn());
                          }
                      }


                      parseYearIfPresent(row, taxesEntity);
                      BigDecimal value = excelParserService.getBigDecimalValue(row, taxesSchema.getValueColumn());
                      if (value != null) {
                          taxesEntity.setValue(value);
                          taxesEntities.add(taxesEntity);
                      } else {
                          componentLog.info("Не заполнено значение на строке " + (row.getRowNum() + 1));
                          continue;
                      }
                  }
                }
                else  {
                    if ((row.getRowNum()) >= taxesSchema.getStartRow2() - 1) {
                        TaxesEntity taxesEntity = new TaxesEntity();
                        taxesEntity.setUuid(UUID.randomUUID());
                        taxesEntity.setHeaderUuid(headerUuid);

                        String analytic = excelParserService
                                .getCellValueByAddress(row, taxesSchema.getAnalyticColumn());
                        String measureUnit = null;

                        if (analytic != null) {

                            // При обнаружении текущей строки в списке header`ов, инициализируем текущий
                            // индекс и добавляем в список имен, переходим к следующей строке
                           // if (isRowMultiindex(row)) {
                           //     headerNames.add(analytic);
                           //     currentHeaderIndex++;
                           //     continue;
                           // }
//
                            // Если мы внутри мультииндекса, то обновляем имя analytic, сцепливая с головным
                            // индексом
                           //if (currentHeaderIndex >= 0 && isInsideMultiindex(row, currentHeaderIndex)) {
                           //    analytic.trim();
                           //    analytic = headerNames.get(currentHeaderIndex) + " " + analytic;
                           //}

                           //// Сцепливаем с аналитикой, если есть единица измерения
                           //if (measureUnit != null) {
                           //    analytic = analytic + " " + measureUnit;
                           //}

                            taxesEntity.setAnalyticUuid(excelParserService
                                    .getUUIDByValue("r_analytic", analytic));

                        } else {
                            if (FsdParserUtil.isInSameRow(row, taxesSchema.getValueColumn2())) {


                                System.out.println(" Строка " + row);

                                throw new ExcelParserException("Ошибка при парсинге параметра ",
                                        taxesSchema.getAnalyticColumn());
                            }
                        }


                       // parseYearIfPresent(row, taxesEntity);
                        BigDecimal value = excelParserService.getBigDecimalValue(row, taxesSchema.getValueColumn2());
                        if (value != null) {
                            taxesEntity.setValue(value);
                            taxesEntities.add(taxesEntity);
                        } else {
                            componentLog.info("Не заполнено значение на строке " + (row.getRowNum() + 1));
                            continue;
                        }
                    }
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return taxesEntities;
    }

    private boolean isRowMultiindex(Row row) {
        if (taxesSchema.getMultiindexHeadRow() != null) {
            int rowNum = row.getRowNum() + 1;
            return taxesSchema.getMultiindexHeadRow().contains(rowNum);
        } else
            return false;
    }

    private boolean isInsideMultiindex(Row row, int currentIndex) throws ExcelParserException {
        List<Integer> bodyIndeces = taxesSchema.getMultiindexBodyRows().get(currentIndex);

        boolean greaterThanLeft = row.getRowNum() >= bodyIndeces.get(0) - 1;
        boolean lessThanRight = row.getRowNum() <= bodyIndeces.get(1) - 1;

        return greaterThanLeft && lessThanRight;
    }

    private void parseYearIfPresent(Row row, TaxesEntity taxesEntity) {
        boolean hasYearColumn = taxesSchema.getYear() != null;
        if (hasYearColumn) {
            String stringYear = excelParserService.getCellValueByAddress(row, taxesSchema.getYear());
            if (stringYear != null) {
                // В тестах были ошибки, когда год парсился как число с плавающей точкой...
                int taxYear = parseStringToInt(stringYear);
                taxesEntity.setYear(taxYear);
            }
        }
    }

}
