package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class ProjectStepEntity {
    private UUID uuid;
    private String name;
    private String type;
    private UUID horizon;
    private String projectName;
    private UUID ba;
    private String code;
    private String equipmentName;
}
