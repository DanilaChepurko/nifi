package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PoolGeologyEntity {
    private UUID uuid;
    private UUID baUuid;
    private UUID horizon_uuid;
    private UUID fieldUuid;
    private UUID poolUuid;
    private UUID areaUuid;
    private String poolType;
    private BigDecimal depth;
    private Boolean lowPermThin;
    private Boolean lowPermThick;
}
