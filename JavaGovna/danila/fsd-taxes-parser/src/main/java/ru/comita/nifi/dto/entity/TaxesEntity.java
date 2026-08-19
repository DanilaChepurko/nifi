package ru.comita.nifi.dto.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaxesEntity {
    private UUID uuid;
    private UUID headerUuid;
    private UUID analyticUuid;
    private Integer year;
    private String measureUnit;
    private BigDecimal value;
}
