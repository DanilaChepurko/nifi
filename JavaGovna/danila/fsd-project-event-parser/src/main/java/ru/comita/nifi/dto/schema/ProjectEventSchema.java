package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class ProjectEventSchema {
    private Integer startRow;
    private String  projectStepColumn;
    private String nameColumn;
    private String numberEventColumn;
    private String shortNameColumn;
    private String typeEventColumn;
    private String lvlEventColumn;
    private String prioritizationGroupColumn;
}
