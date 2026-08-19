package ru.comita.nifi.dto.schema;
import lombok.Data;

@Data
public class StranSchema {
    private Integer startRow;
    private String analyticsColumn;
    private String unitColumn;
    private AnalyticsSchema value;


}
