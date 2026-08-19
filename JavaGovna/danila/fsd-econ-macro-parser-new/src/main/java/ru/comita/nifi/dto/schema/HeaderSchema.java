package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class HeaderSchema {
    private String year;
    private String scenario;
    private Integer lastRow;
    private  String fsdSource;
}
