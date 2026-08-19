package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.nifi.dto.entity.LicenceDopInfoEntity;
import ru.comita.nifi.dto.entity.LicenceEntity;
import ru.comita.nifi.dto.schema.LicenceSchema;
import static ru.comita.lib.util.FsdParserUtil.isInSameRow;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FSDLicenceParserService extends FSDParserService {

    private LicenceSchema licenceSchema;

    public FSDLicenceParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap)
            throws JsonProcessingException {
        this.licenceSchema = initSchema(schemaMap.get(SchemaName.LICENCE), form, new TypeReference<>() {
        });
    }

    class LicenceParseResult {
        private final List<LicenceEntity> licences;
        private final List<LicenceDopInfoEntity> dopInfos;

        public LicenceParseResult(List<LicenceEntity> licences, List<LicenceDopInfoEntity> dopInfos) {
            this.licences = licences;
            this.dopInfos = dopInfos;
        }

        public List<LicenceEntity> getLicences() {
            return licences;
        }

        public List<LicenceDopInfoEntity> getDopInfos() {
            return dopInfos;
        }
    }

    public LicenceParseResult parseLicencesAndDopInfo(StreamingSheet sheet) {
        if (licenceSchema == null) {
            return new LicenceParseResult(Collections.emptyList(), Collections.emptyList());
        }

        Iterator<Row> rowIterator = sheet.rowIterator();
        List<LicenceEntity> licenceEntities = new ArrayList<>();
        List<LicenceDopInfoEntity> dopInfoEntities = new ArrayList<>();

        UUID baUuid = null;
        UUID fieldUuid = null;
        String baAddress = licenceSchema.getBa();
        String fieldAddress = licenceSchema.getField();

        int emptyRowsCounter = 0;
        final int EMPTY_ROW_THRESHOLD = 3;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            int rowNum = row.getRowNum();

            if (rowNum >= licenceSchema.getHeaderStartRow() - 1) {
                if (isInSameRow(row, baAddress)) {
                    baUuid = excelParserService.getUUIDByCellAddress(row, "business_associate", "long_name",
                            baAddress);
                }
                if (isInSameRow(row, fieldAddress)) {
                    fieldUuid = excelParserService.getUUIDByCellAddress(row, "field", fieldAddress);
                }
            }
            if (rowNum >= licenceSchema.getTableStartRow() - 1) {
                try {
                    LicenceEntity licence = createLicenceEntityFromRow(row, baUuid, fieldUuid);
                    if (licence == null) {
                        if (emptyRowsCounter < EMPTY_ROW_THRESHOLD) {
                            emptyRowsCounter++;
                            logger.warn("Пропущена пустая строка {} (счётчик: {})", rowNum + 1, emptyRowsCounter);
                            continue;
                        } else {
                            logger.warn("Прекращён парсинг на строке {}. Обнаружено >{} пустых строк", rowNum,
                                    EMPTY_ROW_THRESHOLD);
                            break;
                        }
                    }
                    LicenceDopInfoEntity dopInfo = createLicenceDopInfoEntityFromRow(row, licence.getUuid());
                    licenceEntities.add(licence);
                    dopInfoEntities.add(dopInfo);

                } catch (Exception e) {
                    String errorMsg = "Ошибка при обработке строки " + (rowNum + 1) + ": " + e.getMessage();
                    getErrorLog().add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
        }

        return new LicenceParseResult(licenceEntities, dopInfoEntities);
    }

    private LicenceEntity createLicenceEntityFromRow(
            Row row,
            UUID baUuid,
            UUID fieldUuid) {

        if (licenceSchema == null) {
            return null;
        }

        UUID horizonUuid = excelParserService.getUUIDByCellAddress(
                row, "horizon", licenceSchema.getHorizonColumn());

        String pool = excelParserService.getCellValueByAddress(
                row, licenceSchema.getPoolColumn());
        UUID poolUuid = pool != null
                ? excelParserService.getUUIDByValue(
                        "pool",
                        pool.replace("...", ".").replace("..", "."))
                : null;
        String licenceYearStr = excelParserService.getCellValueByAddress(
                row, licenceSchema.getLicenceYearColumn());

        String licenceCode = excelParserService.getCellValueByAddress(
                row, licenceSchema.getLicenceCodeColumn());
        Integer licenceYear = tryParseInt(licenceYearStr);

        List<Object> fields = Arrays.asList(horizonUuid, poolUuid, licenceYear, licenceCode);
        long nullCount = fields.stream().filter(Objects::isNull).count();
        boolean rowIsEmpty = (nullCount == fields.size());
        boolean hasPartialNulls = (nullCount > 0) && !rowIsEmpty;

        if (rowIsEmpty) {
            return null;
        }
        if (hasPartialNulls) {
            throw new GroupedException("Некорректные значения НСИ на строке " + (row.getRowNum() + 1));
        }

        LicenceEntity entity = new LicenceEntity();
        entity.setUuid(UUID.randomUUID());
        entity.setBaUuid(baUuid);
        entity.setFieldUuid(fieldUuid);
        entity.setHorizonUuid(horizonUuid);
        entity.setPoolUuid(poolUuid);
        entity.setLicenceYear(licenceYear);
        entity.setLicenceCode(licenceCode);

        return entity;
    }

    private LicenceDopInfoEntity createLicenceDopInfoEntityFromRow(Row row, UUID licenceUuid) {
        if (licenceSchema == null) {
            return null;
        }

        Integer devStartYear = tryParseInt(
                excelParserService.getCellValueByAddress(row, licenceSchema.getDevStartYearColumn()));
        Integer turon1PctYear = tryParseInt(
                excelParserService.getCellValueByAddress(row, licenceSchema.getTuron1PctYearColumn()));
        Integer oilLicenseYear = tryParseInt(
                excelParserService.getCellValueByAddress(row, licenceSchema.getOilLicenseYearColumn()));
        Integer oilGeoLicenseYear = tryParseInt(
                excelParserService.getCellValueByAddress(row, licenceSchema.getOilGeoLicenseYearColumn()));
        Integer oil1PctYear = tryParseInt(
                excelParserService.getCellValueByAddress(row, licenceSchema.getOil1PctYearColumn()));
        BigDecimal oilRecovery2011 = excelParserService.getBigDecimalValue(row,
                licenceSchema.getOilRecovery2011Column());
        BigDecimal oilKkanLimit = excelParserService.getBigDecimalValue(row, licenceSchema.getOilKkanLimitColumn());
        Integer oilKkanLastYear = tryParseInt(
                excelParserService.getCellValueByAddress(row, licenceSchema.getOilKkanLastYearColumn()));
        UUID subsoilAreaUuid = excelParserService.getUUIDByCellAddress(row, "subsoil_area",
                licenceSchema.getSubsoilAreaUuidColumn());

        LicenceDopInfoEntity entity = new LicenceDopInfoEntity();
        entity.setHorizonDocumentUuid(licenceUuid);
        entity.setDevStartYear(devStartYear);
        entity.setTuron1PctYear(turon1PctYear);
        entity.setOilLicenseYear(oilLicenseYear);
        entity.setOilGeoLicenseYear(oilGeoLicenseYear);
        entity.setOil1PctYear(oil1PctYear);
        entity.setOilRecovery2011(oilRecovery2011);
        entity.setOilKkanLimit(oilKkanLimit);
        entity.setOilKkanLastYear(oilKkanLastYear);
        entity.setSubsoilAreaUuid(subsoilAreaUuid);

        return entity;
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
