package ru.comita.nifi.dto.schema;

import java.util.List;

import lombok.Data;

@Data
public class TaxesSchema {
    private Integer startRow;
    private String analyticColumn;
    private String measureColumn;
    private String valueColumn;
    private List<Integer> multiindexHeadRow;
    private List<List<Integer>> multiindexBodyRows;
    private String year;
    private Integer startRow2;
    private String valueColumn2;
}