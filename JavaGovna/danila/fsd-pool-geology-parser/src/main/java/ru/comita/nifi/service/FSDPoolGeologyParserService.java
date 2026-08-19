package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.nifi.dto.entity.PoolGeologyEntity;
import ru.comita.nifi.dto.schema.PoolGeologySchema;
import static ru.comita.lib.util.FsdParserUtil.isInSameRow;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class FSDPoolGeologyParserService extends FSDParserService {

    private PoolGeologySchema poolGeologySchema;

    public FSDPoolGeologyParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap)
            throws JsonProcessingException {
        this.poolGeologySchema = initSchema(schemaMap.get(SchemaName.POOL_GEOLOGY), form, new TypeReference<>() {
        });
    }

    public List<PoolGeologyEntity> createPoolGeologyEntities(StreamingSheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<PoolGeologyEntity> poolGeologyEntities = new ArrayList<>();
        if (poolGeologySchema == null) {
            return poolGeologyEntities;
        }
        String baAdress = poolGeologySchema.getBa();
        String fieldAdress = poolGeologySchema.getField();
        UUID baUuid = null;
        UUID fieldUuid = null;

        int emptyRowsCounter = 0;
        int threshold = 3;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            try {
                // получаем значения шапки отдельно от создания сущностей
                if ((row.getRowNum()) >= poolGeologySchema.getHeaderStartRow() - 1) {
                    if (isInSameRow(row, baAdress)) {
                        baUuid = excelParserService.getUUIDByCellAddress(row, "business_associate", "long_name",
                                baAdress);
                    }
                    if (isInSameRow(row, fieldAdress)) {
                        fieldUuid = excelParserService.getUUIDByCellAddress(row, "field", fieldAdress);
                    }
                }

                if ((row.getRowNum()) >= poolGeologySchema.getTableStartRow() - 1) {
                    PoolGeologyEntity poolGeologyEntity = new PoolGeologyEntity();

                    poolGeologyEntity.setUuid(UUID.randomUUID());
                    poolGeologyEntity.setBaUuid(baUuid);
                    poolGeologyEntity.setFieldUuid(fieldUuid);

                    UUID horizonUuid = excelParserService.getUUIDByCellAddress(
                            row,
                            "horizon",
                            poolGeologySchema.getHorizonColumn());

                    String pool = excelParserService.getCellValueByAddress(
                            row,
                            poolGeologySchema.getPoolColumn());
                    UUID poolUuid = pool != null
                            ? excelParserService.getUUIDByValue(
                                    "pool",
                                    pool
                                            .replace("...", ".")
                                            .replace("..", "."))
                            : null;

                    UUID areaUuid = excelParserService.getUUIDByCellAddress(
                            row,
                            "area",
                            poolGeologySchema.getAreaColumn());
                    String poolType = excelParserService.getCellValueByAddress(
                            row,
                            poolGeologySchema.getPoolTypeColumn());
                    BigDecimal depth = excelParserService.getBigDecimalValue(
                            row,
                            poolGeologySchema.getDepthColumn());
                    Boolean lowPermThin = convertToBool(
                            excelParserService.getCellValueByAddress(
                                    row,
                                    poolGeologySchema.getLowPermThinColumn()));
                    Boolean lowPermThick = convertToBool(
                            excelParserService.getCellValueByAddress(
                                    row,
                                    poolGeologySchema.getLowPermThickColumn()));

                    List<Object> fields = Arrays.asList(
                            horizonUuid,
                            poolUuid,
                            areaUuid,
                            poolType,
                            depth,
                            lowPermThin,
                            lowPermThick);

                    long nullCount = fields.stream().filter(Objects::isNull).count();
                    boolean rowIsEmpty = (nullCount == fields.size());
                    boolean hasPartialNulls = (nullCount > 0) && !rowIsEmpty;

                    if (rowIsEmpty) {
                        if (emptyRowsCounter <= threshold) {
                            emptyRowsCounter++;
                            logger.warn("Пропущена пустая строка " + (row.getRowNum() + 1) + " счётчик: " +
                                    emptyRowsCounter);
                            continue;
                        } else {
                            logger.warn("Прекращен парсинг файла на строке " + row.getRowNum()
                                    + ". Обнаружено больше 3 пустых строк");
                            break;
                        }
                    } else if (hasPartialNulls) {
                        throw new GroupedException(
                                "Обнаружены некорректные значения в НСИ на строке "
                                        + (row.getRowNum() + 1));
                    } else {
                        // Все поля заполнены — сохраняем
                        poolGeologyEntity.setHorizon_uuid(horizonUuid);
                        poolGeologyEntity.setPoolUuid(poolUuid);
                        poolGeologyEntity.setAreaUuid(areaUuid);
                        poolGeologyEntity.setPoolType(poolType);
                        poolGeologyEntity.setDepth(depth);
                        poolGeologyEntity.setLowPermThin(lowPermThick);
                        poolGeologyEntity.setLowPermThick(lowPermThick);

                        poolGeologyEntities.add(poolGeologyEntity);
                    }
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return poolGeologyEntities;
    }

    private Boolean convertToBool(String value) {
        if (value == null) {
            return null;
        }

        switch (value.toLowerCase()) {
            case "да":
                return true;
            case "нет":
                return false;
            default:
                return null;
        }

    }
}
