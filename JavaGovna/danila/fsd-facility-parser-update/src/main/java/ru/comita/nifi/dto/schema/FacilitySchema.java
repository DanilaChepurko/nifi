package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class FacilitySchema {
    private Integer startRow;
    private String field;
    private String horizonColumn;
    private String horizonAreaColumn;
    private String nameColumn;
    private String dkcColumn;
}
