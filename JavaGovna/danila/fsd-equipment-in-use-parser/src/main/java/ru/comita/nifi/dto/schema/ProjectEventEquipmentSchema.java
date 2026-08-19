package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class ProjectEventEquipmentSchema {
    private Integer startRow;
    private String  projectEventColumn;
    private String projectEquipmentColumn;
    private String measureUnitColumn;
    private String physicalVolumeColumn;
    private String estimatedCostColumn;
    private String yearColumn;
    private String functionalGroupColumn;

}
