package ru.comita.nifi.dto.schema;

import lombok.Data;

import java.util.Map;

@Data
public class VolSummarySchema {
    private Integer startRow;
    private String yearColumn;
    private String periodColumn;
    private AnalyticsSchema analytics;
    private Map<String, String> additionalAnalytics;
}
