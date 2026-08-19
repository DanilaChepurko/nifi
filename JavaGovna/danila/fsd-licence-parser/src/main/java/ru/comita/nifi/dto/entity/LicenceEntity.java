package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class LicenceEntity {
    private UUID uuid;
    private UUID baUuid;
    private UUID horizonUuid;
    private UUID fieldUuid;
    private UUID poolUuid;
    private Integer licenceYear;
    private String licenceCode;
}
