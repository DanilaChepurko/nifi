package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class OptionsSchema {
    private Integer startRow;
    private String projectStepColumn;
    private String startDateColumn;
    private String projectVolumeColumn;
    private String projectEquipmentColomn;
    private String edIzmerColumn;
    private String periodType;
    private String comentColomn;
}
