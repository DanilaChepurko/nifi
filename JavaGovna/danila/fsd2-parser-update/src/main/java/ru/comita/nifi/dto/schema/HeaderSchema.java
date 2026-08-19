package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class HeaderSchema {
    private String version;
    private String modelDate;
    private String scenario;
    private String pool;
    private String facility;
    private String horizon;
    private String developmentMethod;
    private String businessAssociate;
    private Integer lastRow;
}
