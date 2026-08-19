package ru.comita.nifi.dto.schema;
import lombok.Data;

@Data
public class OpfSchema {
    private Integer startRow;
    private String nameColomn;
    private AnalyticsSchema value;


}
