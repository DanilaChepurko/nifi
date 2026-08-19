package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class ProjectEventEntity {
    private UUID   uuid;
    private UUID   projectStepUuid;
    private String name;
    private String numberEvent;
    private String shortName;
    private String typeEvent;
    private String lvlEvent;
    private String prioritizationGroup;
}
