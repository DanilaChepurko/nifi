package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEntity;
import ru.comita.nifi.dto.entity.HeaderMetaInfEntity;
import ru.comita.nifi.dto.entity.VolSummaryEntity;
import ru.comita.nifi.service.FSD5ParserService.HeaderParseResult;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class FSD5ExcelLoaderService extends FSDExcelLoaderService<FSD5ParserService> {

    public FSD5ExcelLoaderService(String sheetNum,
                                  String formCell,
                                  Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSD5ParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSD5ParserService fsd5ParserService = fsdParserService;
        HeaderEntity headerEntity;

        componentLog.info("Creating header entity");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            HeaderParseResult headerParseResult = fsd5ParserService.createHeaderParseResult(sheet, file.getName());
            
            headerEntity = headerParseResult.headerEntity;
            clearPreviousRecords(headerEntity);
            componentLog.info("Loading header entity");
            loadHeaderEntity(headerEntity);

            HeaderMetaInfEntity headerMetaInfEntity = headerParseResult.headerMetaInfEntity;
            loadHeaderMetaInfEntity(headerMetaInfEntity);

        }

        componentLog.info("Creating vol summary entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            List<VolSummaryEntity> volSummaryEntities = fsd5ParserService
                    .createVolSummaryEntities(sheet, headerEntity);
            componentLog.info("Loading vol summary entities: " + volSummaryEntities.size());
            loadVolSummaryEntities(volSummaryEntities);
        
        }

        componentLog.info("Done, header_uuid: " + headerEntity.getUuid());
    }

    private void clearPreviousRecords(HeaderEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = String.format("SELECT uuid from header_development where " +
                        "scenario %s " +
                        "and version_uuid %s " +
                        "and version_plan_uuid %s " +
                        "and horizon_uuid %s " +
                        "and year %s " +
                        "and dop_scenario %s",
                headerEntity.getScenario() == null ? "is null" : "= '" + headerEntity.getScenario() + "'",
                headerEntity.getVersionUuid() == null ? "is null" : "= '" + headerEntity.getVersionUuid() + "'",
                headerEntity.getVersionPlanUuid() == null ? "is null" : "= '" + headerEntity.getVersionPlanUuid() + "'",
                headerEntity.getHorizonUuid() == null ? "is null" : "= '" + headerEntity.getHorizonUuid() + "'",
                headerEntity.getYear() == null ? "is null" : "= '" + headerEntity.getYear() + "'",
                headerEntity.getDopScenario() == null ? "is null" : "= '" + headerEntity.getDopScenario() + "'");
        clearHeaderAndReferences(sql, "header_development", List.of("header_development_meta_inf",
                "pden_vol_summary_development"));
    }

    private void loadHeaderEntity(HeaderEntity headerEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = "INSERT INTO public.header_development(" +
                "uuid, scenario, year, version_uuid, name, created_date, updated_date, horizon_uuid, model_date, version_plan_uuid, fluid_type, " +
                "field_uuid, business_associate_uuid, dop_scenario) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; // ← 14 ?
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, headerEntity.getUuid());
            preparedStatement.setObject(2, headerEntity.getScenario());
            preparedStatement.setObject(3, headerEntity.getYear());
            preparedStatement.setObject(4, headerEntity.getVersionUuid());
            preparedStatement.setObject(5, headerEntity.getName());
            preparedStatement.setObject(6, headerEntity.getCreatedDate());
            preparedStatement.setObject(7, headerEntity.getUpdatedDate());
            preparedStatement.setObject(8, headerEntity.getHorizonUuid());
            preparedStatement.setObject(9, headerEntity.getModelDate());
            preparedStatement.setObject(10, headerEntity.getVersionPlanUuid());
            preparedStatement.setObject(11, headerEntity.getFluidType());
            preparedStatement.setObject(12, headerEntity.getFieldUuid());
            preparedStatement.setObject(13, headerEntity.getBaUuid());
            preparedStatement.setObject(14, headerEntity.getDopScenario());

            preparedStatement.execute();
        }
    }

    private void loadHeaderMetaInfEntity(HeaderMetaInfEntity headerMetaInfEntity) throws SQLException {
        connection.setAutoCommit(false);
        String sql = "INSERT INTO public.header_development_meta_inf(" +
                "header_uuid,author,fill_in_date,sync_scenario,sync_version,sync_compared_scenario,sync_compared_version" +
                ") VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, headerMetaInfEntity.getHeaderUuid());
            preparedStatement.setObject(2, headerMetaInfEntity.getAuthor());
            preparedStatement.setObject(3, headerMetaInfEntity.getFillInDate());
            preparedStatement.setObject(4, headerMetaInfEntity.getSyncScenario());
            preparedStatement.setObject(5, headerMetaInfEntity.getSyncVersion());
            preparedStatement.setObject(6, headerMetaInfEntity.getSyncComparedScenario());
            preparedStatement.setObject(7, headerMetaInfEntity.getSyncComparedVersion());
            preparedStatement.execute();
        }
    }

    private void loadVolSummaryEntities(List<VolSummaryEntity> volSummaryEntities) throws SQLException {
        if (volSummaryEntities == null || volSummaryEntities.isEmpty()) {
            return;
        }

        connection.setAutoCommit(false);
        String sql = "INSERT INTO public.pden_vol_summary_development(" +
                "header_uuid,project_step_uuid,project_event_uuid,equipment_category," +
                "analytics_uuid,period_type_uuid,start_date,value,project_step_group,year" +
                ") VALUES(?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (VolSummaryEntity entity : volSummaryEntities) {
                preparedStatement.setObject(1, entity.getHeaderUuid());
                preparedStatement.setObject(2, entity.getProjectStepUuid());
                preparedStatement.setObject(3, entity.getProjectEventUuid());
                preparedStatement.setObject(4, entity.getEquipmentCategory());
                preparedStatement.setObject(5, entity.getAnalyticsUuid());
                preparedStatement.setObject(6, entity.getPeriodTypeUuid());
                preparedStatement.setObject(7, entity.getStartDate());
                preparedStatement.setObject(8, entity.getValue());
                preparedStatement.setObject(9, entity.getProjectStepGroup());
                preparedStatement.setObject(10, entity.getYear());
                preparedStatement.execute();
            }
        }
    }

 
}
