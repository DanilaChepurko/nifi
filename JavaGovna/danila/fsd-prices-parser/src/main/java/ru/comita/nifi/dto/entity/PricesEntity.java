package ru.comita.nifi.dto.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PricesEntity {
    private UUID uuid;
    private UUID headerUuid;
    private UUID analyticUuid;
    private BigDecimal value;
}
