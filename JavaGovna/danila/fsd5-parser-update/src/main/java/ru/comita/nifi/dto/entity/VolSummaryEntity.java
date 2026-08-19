package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VolSummaryEntity {
    private UUID headerUuid;
    private UUID projectStepUuid;
    private UUID projectEventUuid;
    private String equipmentCategory;
    private UUID analyticsUuid;
    private UUID periodTypeUuid;
    private LocalDateTime startDate;
    private BigDecimal value;
    private Integer year;
    private Integer projectStepGroup;
}
