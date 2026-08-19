package ru.comita.nifi.dto.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MacroEntity {
    private UUID uuid;
    private UUID headerUuid;
    private UUID analyticUuid  ;
    private Integer year;
    private UUID scenario;
    private String measureUnit ;
    private BigDecimal value;
    private LocalDateTime createdDate;
}
