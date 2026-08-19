package ru.comita.nifi.dto.entity;

import lombok.Data;
import java.util.UUID;

@Data
public class FluidTypesEntity {
    private UUID uuid;
    private UUID baUuid;
    private UUID horizon_uuid;
    private String fluidType;
    private UUID fieldUuid;
}
