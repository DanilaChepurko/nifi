package ru.comita.nifi.dto.schema;

import lombok.Data;

import java.util.List;

@Data
public class AnalyticsSchema {
    private String startColumn;
    private List<String> values;
}
