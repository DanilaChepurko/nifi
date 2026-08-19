package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProjectEquipmentEntity {
    private UUID   uuid;
    private String name;
    private BigDecimal termBu;
    private BigDecimal  termNu;
    private BigDecimal depricationPremium;
    private BigDecimal termExpenses;
    private String commissioning;
}

