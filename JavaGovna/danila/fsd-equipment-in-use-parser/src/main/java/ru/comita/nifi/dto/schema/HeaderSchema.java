package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class HeaderSchema {
    private String year;
    private String businessAssociate;
    private String field;
    private String version;
    private String dateOfRelevance;
    private Integer lastRow;
    private String fsdSource;
}
