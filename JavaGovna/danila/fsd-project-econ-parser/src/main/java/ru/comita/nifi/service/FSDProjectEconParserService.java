package ru.comita.nifi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pjfanning.xlsx.impl.StreamingSheet;
import org.apache.poi.ss.usermodel.Row;
import ru.comita.lib.dto.SchemaName;
import ru.comita.lib.exception.GroupedException;
import ru.comita.lib.service.ExcelParserService;
import ru.comita.lib.service.FSDParserService;
import ru.comita.nifi.dto.entity.ProjectStepEconEntity;
import ru.comita.nifi.dto.schema.ProjectEconSchema;
import ru.comita.nifi.dto.entity.HeaderEconomEntity;
import ru.comita.nifi.dto.schema.HeaderEconomSchema;
import static ru.comita.lib.util.FsdParserUtil.isInSameRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class FSDProjectEconParserService extends FSDParserService {
    protected class ParseResult{
        protected HeaderEconomEntity headerEconomEntity;
        protected List<ProjectStepEconEntity> projectEconEntities;

        protected ParseResult(HeaderEconomEntity headerEconomEntity, List<ProjectStepEconEntity> projectEconEntities){
            this.headerEconomEntity = headerEconomEntity;
            this.projectEconEntities = projectEconEntities;
        }
    }
    private ProjectEconSchema projectEconSchema;

    private HeaderEconomSchema headerEconom;

    public FSDProjectEconParserService(ExcelParserService excelParserService) {
        super(excelParserService);
    }

    public ParseResult createParseResult(StreamingSheet sheet){
        if (projectEconSchema == null || headerEconom == null){
            return new ParseResult(null,  new ArrayList<>());
        }

        List<ProjectStepEconEntity> projectEconEntities = new ArrayList<>();
        HeaderEconomEntity headerEconomEntity = new HeaderEconomEntity();
        headerEconomEntity.setUuid(UUID.randomUUID());
        headerEconomEntity.setCreatedDate(LocalDateTime.now());
        headerEconomEntity.setFsdSource(headerEconom.getFsdSource());

        Iterator<Row> rowIterator = sheet.rowIterator();

        int emptyRowsNum = 0;
        final int EMPTY_ROWS_THRESHOLD = 3;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            int rowNum = row.getRowNum();

            if ((rowNum > headerEconom.getStartRow() - 1) && (rowNum < projectEconSchema.getStartRow())){
                modifyHeaderWithRowValues(row, headerEconomEntity);
            }
            if (rowNum >= projectEconSchema.getStartRow() - 1){
                ProjectStepEconEntity projectEconEntity = createProjectEconEntity(row, headerEconomEntity);
                if (projectEconEntity != null)
                    projectEconEntities.add(projectEconEntity);
                else {
                    logger.warn("Пропущена пустая строка {} (счётчик: {})", rowNum + 1, emptyRowsNum);
                    emptyRowsNum++;
                }
            }

            if (emptyRowsNum >= EMPTY_ROWS_THRESHOLD){
                logger.warn("Прекращён парсинг на строке {}. Обнаружено >{} пустых строк", rowNum,
                        EMPTY_ROWS_THRESHOLD);
                break;
            }
        }
        return new ParseResult(headerEconomEntity, projectEconEntities);
    }

    public void modifyHeaderWithRowValues(Row row, HeaderEconomEntity headerEconomEntity) {

        String baAdress = headerEconom.getBa();
        if (isInSameRow(row, baAdress)) {
            headerEconomEntity.setBaUuid(
                    excelParserService.getUUIDByCellAddress(row, "business_associate", "long_name", baAdress));

        }
        String fieldAdress = headerEconom.getField();
        if (isInSameRow(row, fieldAdress)) {
            headerEconomEntity.setFieldUuid(
                    excelParserService.getUUIDByCellAddress(row, "field", "name", fieldAdress));

        }

        String headerYear = headerEconom.getYear();
        if (isInSameRow(row, headerYear)) {
            Integer year = tryParseInt(
                    excelParserService.getCellValueByAddress(row, headerYear));
            headerEconomEntity.setYear(year);
        }
    }

    public ProjectStepEconEntity createProjectEconEntity(Row row, HeaderEconomEntity headerEconomEntity) {
        UUID headerUuid = headerEconomEntity.getUuid();

        ProjectStepEconEntity projectEconEntity = new ProjectStepEconEntity();
        projectEconEntity.setUuid(UUID.randomUUID());
        projectEconEntity.setHeaderUuid(headerUuid);

        UUID projectUuid = excelParserService.getUUIDByCellAddress(row,"project_step",projectEconSchema.getProjectStepColumn());
        UUID scenario = excelParserService.getUUIDByCellAddress(row,"r_scenario", projectEconSchema.getScenarioColumn());
        String constructionType = excelParserService.getCellValueByAddress(row, projectEconSchema.getConstructionTypeColumn());
        String functionalGroup = excelParserService.getCellValueByAddress(row, projectEconSchema.getFunctionalGroupColumn());
        String investProgram = excelParserService.getCellValueByAddress(row,projectEconSchema.getInvestProgramColumn());
        String complexReconstructionProgram = excelParserService.getCellValueByAddress(row,projectEconSchema.getComplexReconstructionProgramColumn());

        String priority = excelParserService.getCellValueByAddress(row,projectEconSchema.getPriorityColumn());
        UUID projectDependencyUUID = excelParserService.getUUIDByCellAddress(row, "project_step","code", projectEconSchema.getProjectStepDependencyColumn());
        Integer pirStartYear = tryParseInt(
                excelParserService.getCellValueByAddress(row, projectEconSchema.getPirStartYearColumn()));
        Integer pirEndYear = tryParseInt(
                excelParserService.getCellValueByAddress(row, projectEconSchema.getPirEndYearColumn())
        );
        Integer smrStartYear = tryParseInt(
                excelParserService.getCellValueByAddress(row,projectEconSchema.getSmrStartYearColumn())
        );
        Integer smrEndYear = tryParseInt(
                excelParserService.getCellValueByAddress(row,projectEconSchema.getSmrEndYearColumn())
        );
        Integer projectStepCompletionYear = tryParseInt(
                excelParserService.getCellValueByAddress(row,projectEconSchema.getProjectStepCompletionYearColumn())
        );
        BigDecimal value = excelParserService.getBigDecimalValue(row,
                projectEconSchema.getValueColumn());


        //Проверяем обязательные поля
        Object[][] fields = {
                {"UUID этапа проекта", projectUuid},
              //  {"Сценарий", scenario},
                {"Тип строительства", constructionType},
                {"Функциональная группа", functionalGroup},
               // {"Инвестиционная программа", investProgram},
                {"Приоритет", priority},
                {"Значение", value}
        };

        List<String> nullFieldNames = Stream.of(fields)
                .filter(pair -> pair[1] == null)
                .map(pair -> (String) pair[0])
                .collect(Collectors.toList());

        int totalCount = fields.length;
        int nullCount = nullFieldNames.size();

        if (nullCount == totalCount) {
            return null;
        } else if (nullCount > 0) {
            throw new GroupedException("На строке " + (row.getRowNum() + 1) + " обнаружены некорректные значения: " + nullFieldNames);
        }

        projectEconEntity.setProjectStepUuid(projectUuid);
        projectEconEntity.setScenario(scenario);
        projectEconEntity.setConstructionType(constructionType.trim());
        projectEconEntity.setFunctionalGroup(functionalGroup.trim());
        projectEconEntity.setInvestProgram(investProgram.trim());
        projectEconEntity.setPriority(priority.trim());
        projectEconEntity.setProjectStepDependency(projectDependencyUUID);
        projectEconEntity.setPirStartYear(pirStartYear);
        projectEconEntity.setPirEndYear(pirEndYear);
        projectEconEntity.setSmrStartYear(smrStartYear);
        projectEconEntity.setSmrEndYear(smrEndYear);
        projectEconEntity.setAnalyticUuid(
                excelParserService.getUUIDByValue( "r_analytic", projectEconSchema.getAnalyticName())
        );
        projectEconEntity.setValue(value);
        projectEconEntity.setComplexReconstructionProgram(complexReconstructionProgram);
        projectEconEntity.setProjectStepCompletionYear(projectStepCompletionYear);

        return projectEconEntity;
    }

    @Override
    protected void initSchemas(String form, Iterator<Row> rowIterator, Map<SchemaName, String> schemaMap)
            throws JsonProcessingException {
        this.projectEconSchema = initSchema(schemaMap.get(SchemaName.PROJECT_ECON), form, new TypeReference<>() {
        });
        this.headerEconom = initSchema(schemaMap.get(SchemaName.HEADER), form, new TypeReference<>() {
        });
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

