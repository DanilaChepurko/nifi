package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class HeaderSchema {
    private String year;
    private String businessAssociate;
    private String field;
    private String horizon;
    private Integer lastRow;
    private  String fsdSource;
}
