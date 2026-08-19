package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class InvestProgramSchema {
    private Integer startRow;
    private String  fieldColumn;
    private String  horizonColumn;
    private String  horizonAreaColumn;
    private String  sourceIp;
    private String  constructionNamberColumn;
    private String  constructionCodColumn;
    private String  equipmentNameColumn;
    private String  volumeEquipmentColumn;
    private String  volumeUnitColumn;
    private String  startYearAssociate;
    private String  startQuarterColumn;
}
