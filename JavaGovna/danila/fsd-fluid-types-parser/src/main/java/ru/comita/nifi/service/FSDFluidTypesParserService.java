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
import ru.comita.nifi.dto.entity.FluidTypesEntity;
import ru.comita.nifi.dto.schema.FluidTypesSchema;
import static ru.comita.lib.util.FsdParserUtil.isInSameRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FSDFluidTypesParserService extends FSDParserService {

    private FluidTypesSchema fluidTypesSchema;

    public FSDFluidTypesParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap)
            throws JsonProcessingException {
        this.fluidTypesSchema = initSchema(schemaMap.get(SchemaName.FLUID_TYPES), form, new TypeReference<>() {
        });
    }

    public List<FluidTypesEntity> createFluidTypesEntities(StreamingSheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<FluidTypesEntity> fluidTypesEntities = new ArrayList<>();
        if (fluidTypesSchema == null) {
            return fluidTypesEntities;
        }
        String baAdress = fluidTypesSchema.getBa();
        String fieldAdress = fluidTypesSchema.getField();
        UUID baUuid = null;
        UUID fieldUuid = null;

        int emptyRowsCounter = 0;
        int threshold = 3;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            try {
                // получаем значения шапки отдельно от создания сущностей
                if ((row.getRowNum()) >= fluidTypesSchema.getHeaderStartRow() - 1) {
                    if (isInSameRow(row, baAdress)) {
                        baUuid = excelParserService.getUUIDByCellAddress(row, "business_associate", "long_name",
                                baAdress);
                    }
                    if (isInSameRow(row, fieldAdress)) {
                        fieldUuid = excelParserService.getUUIDByCellAddress(row, "field", fieldAdress);
                    }
                }

                if ((row.getRowNum()) >= fluidTypesSchema.getTableStartRow() - 1) {
                    FluidTypesEntity fluidTypesEntity = new FluidTypesEntity();

                    fluidTypesEntity.setUuid(UUID.randomUUID());
                    fluidTypesEntity.setBaUuid(baUuid);
                    fluidTypesEntity.setFieldUuid(fieldUuid);

                    UUID horizon_uuid = excelParserService.getUUIDByCellAddress(
                            row,
                            "horizon",
                            fluidTypesSchema.getHorizonColumn());
                    String fluidType = excelParserService.getCellValueByAddress(
                            row,
                            fluidTypesSchema.getFluidTypeColumn());

                    if (horizon_uuid != null && fluidType != null) {
                        fluidTypesEntity
                                .setHorizon_uuid(
                                        horizon_uuid);
                        fluidTypesEntity.setFluidType(fluidType);

                        fluidTypesEntities.add(fluidTypesEntity);
                    } else if (horizon_uuid == null && fluidType == null) {
                        emptyRowsCounter++;
                        logger.warn("Пропущено значение в строке = " + (row.getRowNum() + 1) + ".Счетчик пустых строк "
                                + emptyRowsCounter);
                    }
                    if (emptyRowsCounter >= threshold)
                        break;
                }
            } catch (GroupedException e) {
                getErrorLog().add(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
        return fluidTypesEntities;
    }

}
