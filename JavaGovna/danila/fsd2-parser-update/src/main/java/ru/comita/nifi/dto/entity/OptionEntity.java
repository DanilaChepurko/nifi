package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OptionEntity {
    private UUID headerUuid;
    private UUID projectStepUuid;
    private UUID projectEquipment;
    private LocalDateTime startDate;
    private UUID periodTypeUuid;
    private BigDecimal volume;
    private String edIzmer;
    private String coment;
}
