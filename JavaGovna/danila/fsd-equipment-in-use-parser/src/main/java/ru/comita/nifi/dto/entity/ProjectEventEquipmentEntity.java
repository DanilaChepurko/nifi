package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProjectEventEquipmentEntity {
    private UUID   uuid;
    private UUID   headerEconUuid;
    private UUID   projectEventUuid;
    private UUID   projectEquipmentUuid;
    private Integer year;
    private String measureUnit ;
    private BigDecimal physicalVolume ;
    private BigDecimal estimatedCost ;
    private String  functionalGroup;
}
