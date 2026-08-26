package ru.comita.nifi.service;

import com.github.pjfanning.xlsx.impl.StreamingSheet;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDExcelLoaderService;
import ru.comita.lib.util.FsdParserUtil;
import ru.comita.nifi.dto.entity.HeaderEconomEntity;
import ru.comita.nifi.dto.entity.ProjectStepEconEntity;
import ru.comita.nifi.service.FSDProjectEconParserService.ParseResult;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class FSDProjectEconExcelLoaderService extends FSDExcelLoaderService<FSDProjectEconParserService> {

    public FSDProjectEconExcelLoaderService(String sheetNum,
                                            String formCell,
                                            Connection connection) {
        super(sheetNum, formCell, connection);
    }

    @Override
    protected void initParser(ExcelParserService excelParserService) {
        this.fsdParserService = new FSDProjectEconParserService(excelParserService);
    }

    @Override
    protected void createAndLoadEntities(File file, Integer sheetNum) throws IOException, SQLException {
        FSDProjectEconParserService fsdProjectEconParserService = fsdParserService;
        HeaderEconomEntity headerEntity;
        List<ProjectStepEconEntity> projectEconEntities;

        componentLog.info("Parsing header and project_step_econ entities");
        try (StreamingWorkbook workbook = FsdParserUtil.getWorkbookFromFile(file)) {
            StreamingSheet sheet = (StreamingSheet) workbook.getSheetAt(sheetNum);
            ParseResult parseResult = fsdProjectEconParserService.createParseResult(sheet);
            headerEntity = parseResult.headerEconomEntity;
            projectEconEntities = parseResult.projectEconEntities;

            componentLog.info("Parsed header and project_step_econ enitites");
            clearPreviousRecords(headerEntity);

            loadHeaderEntity(headerEntity);
            componentLog.info("Loaded header entity with UUID: " + headerEntity.getUuid());

            loadProjectEconEntities(projectEconEntities);
            componentLog.info("Loaded project_econ entites: " + projectEconEntities.size());
        }

    }

    private void clearPreviousRecords(HeaderEconomEntity headerEntity) throws SQLException {
        if (headerEntity == null || headerEntity.getFsdSource() == null) {
            return;
        }

        connection.setAutoCommit(false);

        String condition =
                "fsd_source = ? " +
                        "AND year IS NOT DISTINCT FROM ? " +
                        "AND field_uuid IS NOT DISTINCT FROM ? ";

        String deleteProjectEconSql =
                "DELETE FROM public.project_step_econ " +
                        "WHERE header_uuid IN (" +
                        "SELECT uuid FROM public.header_econom WHERE " + condition +
                        ")";

        String deleteHeaderEconomSql =
                "DELETE FROM public.header_econom WHERE " + condition;

        try (PreparedStatement ps1 = connection.prepareStatement(deleteProjectEconSql);
             PreparedStatement ps2 = connection.prepareStatement(deleteHeaderEconomSql)) {

            String fsdSource = headerEntity.getFsdSource();
            Integer year = headerEntity.getYear();
            UUID fiedlUuid = headerEntity.getFieldUuid();

            ps1.setString(1, fsdSource);
            ps1.setObject(2, year);
            ps1.setObject(3, fiedlUuid);

            ps2.setString(1, fsdSource);
            ps2.setObject(2, year);
            ps2.setObject(3, fiedlUuid);

            ps1.execute();
            ps2.execute();

            connection.commit();
        } catch (SQLException e) {
            componentLog.error(
                    "Ошибка при очистке записей: fsd_source='{}', ba={}, year={}",
                    headerEntity.getFsdSource(),
                    headerEntity.getBaUuid(),
                    headerEntity.getYear(),
                    e
            );
            connection.rollback();
            throw e;
        }
    }

    private void loadProjectEconEntities(List<ProjectStepEconEntity> projectEconEntities) throws SQLException {
        if (projectEconEntities.isEmpty()) {
            return;
        }

        connection.setAutoCommit(false);

        String insertSql = "INSERT INTO public.project_step_econ (" +
                "uuid, " +
                "header_uuid, " +
                "project_step_uuid, " +
                "invest_program, " +
                "r_scenario, " +
                "construction_type, " +
                "functional_group, " +
                "priority, " +
                "project_dependency, " +
                "pir_start_year, " +
                "pir_end_year, " +
                "smr_start_year, " +
                "smr_end_year, " +
                "analytic_uuid, " +
                "value," +
                "project_step_completion_year," +
                "complex_reconstruction_program" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";

        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            for (ProjectStepEconEntity entity : projectEconEntities) {
                ps.setObject(1, entity.getUuid());
                ps.setObject(2, entity.getHeaderUuid());
                ps.setObject(3, entity.getProjectStepUuid());
                ps.setString(4, entity.getInvestProgram());
                ps.setObject(5, entity.getScenario());
                ps.setString(6, entity.getConstructionType());
                ps.setString(7, entity.getFunctionalGroup());
                ps.setString(8, entity.getPriority());
                ps.setObject(9, entity.getProjectStepDependency());
                ps.setObject(10, entity.getPirStartYear());
                ps.setObject(11, entity.getPirEndYear());
                ps.setObject(12, entity.getSmrStartYear());
                ps.setObject(13, entity.getSmrEndYear());
                ps.setObject(14, entity.getAnalyticUuid());
                ps.setBigDecimal(15, entity.getValue());
                ps.setObject(16, entity.getProjectStepCompletionYear());
                ps.setObject(17, entity.getComplexReconstructionProgram());

                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
        }
    }

    private void loadHeaderEntity(HeaderEconomEntity headerEntity) throws SQLException {
        if (headerEntity == null) {
            return;
        }

        connection.setAutoCommit(false);
        String sql = "INSERT INTO public.header_econom(" +
                "uuid, " +
                "fsd_source, " +
                "scenario, " +
                "year, " +
                "created_date, " +
                "field_uuid" +
                ") VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, headerEntity.getUuid());
            ps.setString(2, headerEntity.getFsdSource());
            ps.setObject(3, headerEntity.getScenarioUuid());
            ps.setObject(4, headerEntity.getYear());
            ps.setObject(5, headerEntity.getCreatedDate());
            ps.setObject(6, headerEntity.getFieldUuid());

            ps.execute();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

}
