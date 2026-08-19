package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class HeaderEconomSchema {
    private Integer startRow;
    private Integer lastRow;
    private String fsdSource;
    private String ba;
    private String year;
}
