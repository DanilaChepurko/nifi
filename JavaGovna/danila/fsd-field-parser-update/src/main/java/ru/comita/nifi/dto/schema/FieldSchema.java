package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class FieldSchema {
    private Integer startRow;
    private String nameColumn;
    private String areaColumn;
    private String typeColumn;
    private String licenseColumn;
    private String esgColumn;
    private String businessAssociate;
    private String yearOpenColumn;
    private String producSaleColumn;
}
