package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class PoolGeologySchema {
    private Integer headerStartRow;
    private Integer tableStartRow;
    private String field;
    private String ba;
    private String horizonColumn;
    private String poolColumn;
    private String poolTypeColumn;
    private String depthColumn;
    private String lowPermThinColumn;
    private String lowPermThickColumn;
    private String areaColumn;
}