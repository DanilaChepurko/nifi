package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.FluidTypesEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.Set;

public class FSDFluidTypesExcelLoaderService extends FSDExcelLoaderService<FSDFluidTypesParserService> {

    public FSDFluidTypesExcelLoaderService(String sheetNum,
            String formCell,
            Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDFluidTypesParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDFluidTypesParserService FSDFluidTypesParserService = fsdParserService;

        componentLog.info("Creating fluidTypes entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<FluidTypesEntity> fluidTypesEntities = FSDFluidTypesParserService.createFluidTypesEntities(sheet);

            Set<UUID> fieldUuids = fluidTypesEntities.stream()
                    .map(FluidTypesEntity::getFieldUuid)
                    .collect(Collectors.toSet());
            clearPreviosRecords(fieldUuids);

            componentLog.info("Loading fluid types entities: " + fluidTypesEntities.size());
            loadFluidTypesEntities(fluidTypesEntities);
        }
        componentLog.info("Fluid Types are loaded");

    }

    private void loadFluidTypesEntities(List<FluidTypesEntity> fluidTypesEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (fluidTypesEntities.isEmpty()) {
            return;
        }

        String insertSql = "INSERT INTO fluid_types(uuid, horizon_uuid, ba_uuid, field_uuid, type) "
                +
                "VALUES(?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (FluidTypesEntity fluidTypeEntity : fluidTypesEntities) {
                preparedStatement.setObject(1, fluidTypeEntity.getUuid());
                preparedStatement.setObject(2, fluidTypeEntity.getHorizon_uuid());
                preparedStatement.setObject(3, fluidTypeEntity.getBaUuid());
                preparedStatement.setObject(4, fluidTypeEntity.getFieldUuid());
                preparedStatement.setObject(5, fluidTypeEntity.getFluidType());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private void clearPreviosRecords(Set<UUID> uuidSet) throws SQLException {
        if (uuidSet == null || uuidSet.isEmpty())
            return;

        String deleteSql = "delete from fluid_types where field_uuid = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
            for (UUID uuid : uuidSet) {
                preparedStatement.setObject(1, uuid);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }

    }
}
