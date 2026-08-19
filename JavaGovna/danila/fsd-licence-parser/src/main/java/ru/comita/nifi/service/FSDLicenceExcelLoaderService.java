package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.LicenceDopInfoEntity;
import ru.comita.nifi.dto.entity.LicenceEntity;
import ru.comita.nifi.service.FSDLicenceParserService.LicenceParseResult;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.Set;

public class FSDLicenceExcelLoaderService extends FSDExcelLoaderService<FSDLicenceParserService> {

    public FSDLicenceExcelLoaderService(String sheetNum,
            String formCell,
            Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDLicenceParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDLicenceParserService FSDLicenceParserService = fsdParserService;

        componentLog.info("Creating licence and dop info entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            LicenceParseResult licenceParseResult = FSDLicenceParserService.parseLicencesAndDopInfo(sheet);
            List<LicenceEntity> licenceEntities = licenceParseResult.getLicences();
            List<LicenceDopInfoEntity> licenceDopInfoEntities = licenceParseResult.getDopInfos();

            Set<UUID> fieldUuids = licenceEntities.stream()
                    .map(LicenceEntity::getFieldUuid)
                    .collect(Collectors.toSet());
            clearPreviosRecords(fieldUuids);

            componentLog.info("Loading licences entities: " + licenceEntities.size());
            loadLicenceEntities(licenceEntities);

            componentLog.info("Loading dop info entities: " + licenceDopInfoEntities.size());
            loadLicenceDopInfoEntities(licenceDopInfoEntities);

        }
        componentLog.info("Done. Licences are loaded");

    }

    private void loadLicenceEntities(List<LicenceEntity> licenceEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (licenceEntities.isEmpty()) {
            return;
        }

        String insertSql = "INSERT INTO horizon_document(uuid, ba_uuid, horizon_uuid, field_uuid, pool_uuid, licence_year, licence_code) "
                +
                "VALUES(?,?,?,?,?,?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (LicenceEntity licenceEntity : licenceEntities) {
                preparedStatement.setObject(1, licenceEntity.getUuid());
                preparedStatement.setObject(2, licenceEntity.getBaUuid());
                preparedStatement.setObject(3, licenceEntity.getHorizonUuid());
                preparedStatement.setObject(4, licenceEntity.getFieldUuid());
                preparedStatement.setObject(5, licenceEntity.getPoolUuid());
                preparedStatement.setInt(6, licenceEntity.getLicenceYear());
                preparedStatement.setString(7, licenceEntity.getLicenceCode());

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private void loadLicenceDopInfoEntities(List<LicenceDopInfoEntity> licenceDopInfoEntities) throws SQLException {
        connection.setAutoCommit(false);
        if (licenceDopInfoEntities.isEmpty()) {
            return;
        }

        String insertSql = "INSERT INTO horizon_document_dop_info (" +
                "horizon_document_uuid, " +
                "dev_start_year, " +
                "turon_1pct_year, " +
                "oil_license_year, " +
                "oil_geo_license_year, " +
                "oil_1pct_year, " +
                "oil_recovery_2011, " +
                "oil_kkan_limit, " +
                "oil_kkan_last_year, " +
                "subsoil_area_uuid" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (LicenceDopInfoEntity licenceDopInfoEntity : licenceDopInfoEntities) {
                preparedStatement.setObject(1, licenceDopInfoEntity.getHorizonDocumentUuid());
                preparedStatement.setInt(2, licenceDopInfoEntity.getDevStartYear());
                preparedStatement.setObject(3, licenceDopInfoEntity.getTuron1PctYear());
                preparedStatement.setObject(4, licenceDopInfoEntity.getOilLicenseYear());
                preparedStatement.setObject(5, licenceDopInfoEntity.getOilGeoLicenseYear());
                preparedStatement.setObject(6, licenceDopInfoEntity.getOil1PctYear());
                preparedStatement.setBigDecimal(7, licenceDopInfoEntity.getOilRecovery2011());
                preparedStatement.setBigDecimal(8, licenceDopInfoEntity.getOilKkanLimit());
                preparedStatement.setObject(9, licenceDopInfoEntity.getOilKkanLastYear());
                preparedStatement.setObject(10, licenceDopInfoEntity.getSubsoilAreaUuid());

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private void clearPreviosRecords(Set<UUID> uuidSet) throws SQLException {
        if (uuidSet == null || uuidSet.isEmpty())
            return;

        String deleteSql = "delete from horizon_document where field_uuid = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
            for (UUID uuid : uuidSet) {
                preparedStatement.setObject(1, uuid);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }

    }
}
