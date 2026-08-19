package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VolSummaryEntity {
    private UUID headerUuid;
    private UUID facilityUuid;
    private UUID poolUuid;
    private UUID horizonUuid;
    private UUID analyticsUuid;
    private UUID periodTypeUuid;
    private LocalDateTime startDate;
    private BigDecimal value;



}
