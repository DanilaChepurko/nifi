package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;
@Data
public class OpfEntity {
    private UUID headerUuid;
    private LocalDateTime createdDate;
    private UUID analyticsUuid;
    private String name ;
    private BigDecimal value;
}
