package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HeaderEntity {
    private UUID uuid;
    private UUID scenario;
    private Integer year;
    private UUID versionUuid;
    private UUID poolUuid;
    private UUID facilityUuid;
    private UUID horizon;
    private UUID baUuid;
    private LocalDateTime modelDate;
    private UUID developmentMethodUuid;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
