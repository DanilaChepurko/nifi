package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.ExcelParserException;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;
import ru.comita.nifi.dto.schema.HeaderSchema;

import ru.comita.nifi.dto.entity.MacroEntity;
import ru.comita.nifi.dto.schema.MacroSchema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;


public class FSDMacroParserService extends FSDParserService {
    private HeaderSchema headerSchema;
    private MacroSchema macroSchema;

    public FSDMacroParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap) throws JsonProcessingException {
        this.headerSchema = initSchema(schemaMap.get(SchemaName.HEADER), form, new TypeReference<>() {
        });
        this.macroSchema = initSchema(schemaMap.get(SchemaName.MACRO), form, new TypeReference<>() {
        });
    }

    public HeaderEntity createHeaderEntity(StreamingSheet sheet, String fileName) {
        HeaderEntity headerEntity = new HeaderEntity();
        headerEntity.setUuid(UUID.randomUUID());
        headerEntity.setName(fileName);
        headerEntity.setCreatedDate(LocalDateTime.now());
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

                if (FsdParserUtil.isInSameRow(row, headerSchema.getScenario())) {

                    UUID Scenario = excelParserService.getUUIDByCellAddress(row, "r_scenario", headerSchema.getScenario());

                    if (Scenario != null) {
                        headerEntity.setScenario(Scenario);
                    } else {
                        throw new ExcelParserException("Не удалось определить сценарий", headerSchema.getScenario());
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


    public List<MacroEntity> createMacroEntities(StreamingSheet sheet, HeaderEntity headerEntity) {
        List<MacroEntity> macroEntities = new ArrayList<>();
        if (macroSchema == null) {
            return macroEntities;
        }

        Iterator<Row> rowIterator = sheet.rowIterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {

                if ((row.getRowNum() + 1) >= macroSchema.getStartRow()) {

                       MacroEntity macroEntity = new MacroEntity();
                        macroEntity.setUuid(UUID.randomUUID());
                        macroEntity.setHeaderUuid(headerEntity.getUuid());
                        String anlytic = excelParserService
                                .getCellValueByAddress(row, macroSchema.getAnalyticColumn());
                        if (anlytic != null) {
                            String measureUnit = excelParserService
                                    .getCellValueByAddress(row, macroSchema.getMeasureColumn());
                            if (measureUnit != null) {
                               anlytic= anlytic+" "+measureUnit;
                            } else {
                                anlytic= anlytic+" "+"%";
                            }

                            macroEntity.setAnalyticUuid(excelParserService
                                    .getUUIDByValue("r_analytic", anlytic));
                        } else {
                            if (FsdParserUtil.isInSameRow(row, macroSchema.getValueColumn())) {
                                throw new ExcelParserException("Не правельный параметр ", macroSchema.getAnalyticColumn());
                            }
                        }

                        BigDecimal value= excelParserService.getBigDecimalValue(row,CellReference.convertColStringToIndex( macroSchema.getValueColumn()));
                        macroEntity.setValue(value);

                        macroEntity.setCreatedDate(LocalDateTime.now());
                        if (value != null) {
                            macroEntities.add(macroEntity);
                        }
                }
            }
            catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return macroEntities;
    }
}