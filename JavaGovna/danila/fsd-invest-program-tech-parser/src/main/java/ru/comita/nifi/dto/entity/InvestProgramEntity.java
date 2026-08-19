package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InvestProgramEntity {
    private UUID uuid;
    private UUID field;
    private UUID horizon;
    private UUID horizonArea;
    private String sourceIp;
    private Integer constructionNamber;
    private String constructionCod;
    private String equipmentName;
    private BigDecimal volumeEquipment;
    private String volumeUnit;
    private LocalDateTime startDate;
}
