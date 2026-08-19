package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class FluidTypesSchema {
    private Integer headerStartRow;
    private Integer tableStartRow;
    private String horizonColumn;
    private String fluidTypeColumn;
    private String field;
    private String ba;
}