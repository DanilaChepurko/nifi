package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class FacilityEntity {
    private UUID uuid;
    private String name;
    private UUID fieldUuid;
    private UUID horizonUuid;
    private UUID horizonAreaUuid;
    private String shortName;
    private String dkc;
}
