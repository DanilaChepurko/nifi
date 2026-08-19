package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class VolSummarySchema {
    private Integer startRow;
    private String projectStepColumn;
    private String projectEventColumn;
    private String equipmentCategoryColumn;
    private AnalyticsSchema analytics;
    private String periodTypeColumn;
    private String yearColumn;
    private String projectStepGroupColumn;
}
