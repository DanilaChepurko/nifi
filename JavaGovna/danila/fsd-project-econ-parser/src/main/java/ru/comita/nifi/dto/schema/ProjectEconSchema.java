package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class ProjectEconSchema {
    private Integer startRow;
    private String uuidColumn;
    private String projectStepColumn;
    private String investProgramColumn;
    private String scenarioColumn;
    private String constructionTypeColumn;
    private String functionalGroupColumn;
    private String priorityColumn;
    private String projectStepDependencyColumn;
    private String pirStartYearColumn;
    private String pirEndYearColumn;
    private String smrStartYearColumn;
    private String smrEndYearColumn;
    private String analyticName;
    private String valueColumn;
}