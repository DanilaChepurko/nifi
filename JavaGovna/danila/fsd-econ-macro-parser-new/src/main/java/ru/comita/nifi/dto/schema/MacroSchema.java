package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class MacroSchema {
    private Integer startRow;
    private String scenario;
    private String year;
    private String analyticColumn;
    private String measureColumn;
    private String valueColumn;

}
