package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HeaderEntity {
    private UUID uuid;
    private UUID baUuid;
    private UUID scenario;
    private Integer year;
    private UUID versionUuid;
    private UUID fieldUuid;
    private UUID poolUuid;
    private UUID versionPlanUuid;
    private UUID horizonUuid;
    private LocalDateTime modelDate;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String fluidType;
    private UUID dopScenario;
}
