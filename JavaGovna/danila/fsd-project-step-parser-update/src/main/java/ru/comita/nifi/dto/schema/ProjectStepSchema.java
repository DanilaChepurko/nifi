package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class ProjectStepSchema {
    private Integer startRow;
    private String nameColumn;
    private String typeColumn;
    private String horizonColumn;
    private String projectNameColumn;
    private String statusColumn;
    private String codeColumn;
    private String businessAssociate;
    private String equipmentNameColumn;
}
