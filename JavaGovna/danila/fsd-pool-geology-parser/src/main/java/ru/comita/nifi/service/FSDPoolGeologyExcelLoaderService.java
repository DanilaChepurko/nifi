package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.PoolGeologyEntity;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.Set;

public class FSDPoolGeologyExcelLoaderService extends FSDExcelLoaderService<FSDPoolGeologyParserService> {

    public FSDPoolGeologyExcelLoaderService(String sheetNum,
            String formCell,
            Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDPoolGeologyParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDPoolGeologyParserService FSDPoolGeologyParserService = fsdParserService;

        componentLog.info("Creating pool geology entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<PoolGeologyEntity> poolGeologyEntities = FSDPoolGeologyParserService.createPoolGeologyEntities(sheet);

            Set<UUID> fieldUuids = poolGeologyEntities.stream()
                    .map(PoolGeologyEntity::getFieldUuid)
                    .collect(Collectors.toSet());
            clearPreviosRecords(fieldUuids);

            componentLog.info("Loading pool geology entities: " + poolGeologyEntities.size());
            loadPoolGeologyEntities(poolGeologyEntities);
        }
        componentLog.info("Pool geology entities are loaded");

    }

    private void loadPoolGeologyEntities(List<PoolGeologyEntity> poolGeologyEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (poolGeologyEntities.isEmpty()) {
            return;
        }

        String insertSql = "INSERT INTO pool_geology(uuid, ba_uuid, horizon_uuid, field_uuid, pool_uuid, area_uuid, pool_type, depth, low_perm_thin, low_perm_thick)"
                +
                "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (PoolGeologyEntity poolGeologyEntity : poolGeologyEntities) {
                preparedStatement.setObject(1, poolGeologyEntity.getUuid());
                preparedStatement.setObject(2, poolGeologyEntity.getBaUuid());
                preparedStatement.setObject(3, poolGeologyEntity.getHorizon_uuid());
                preparedStatement.setObject(4, poolGeologyEntity.getFieldUuid());
                preparedStatement.setObject(5, poolGeologyEntity.getPoolUuid());
                preparedStatement.setObject(6, poolGeologyEntity.getAreaUuid());
                preparedStatement.setObject(7, poolGeologyEntity.getPoolType());
                preparedStatement.setObject(8, poolGeologyEntity.getDepth());
                preparedStatement.setObject(9, poolGeologyEntity.getLowPermThin());
                preparedStatement.setObject(10, poolGeologyEntity.getLowPermThick());

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private void clearPreviosRecords(Set<UUID> uuidSet) throws SQLException {
        if (uuidSet == null || uuidSet.isEmpty())
            return;

        String deleteSql = "delete from pool_geology where field_uuid = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
            for (UUID uuid : uuidSet) {
                preparedStatement.setObject(1, uuid);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }

    }
}
