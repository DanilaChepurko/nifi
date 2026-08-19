package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class PricesSchema {
    private Integer startRow;
    private String analyticColumn;
    private String measureColumn;
    private String fsdSource;
    private String valueColumn;
    private String year;
}