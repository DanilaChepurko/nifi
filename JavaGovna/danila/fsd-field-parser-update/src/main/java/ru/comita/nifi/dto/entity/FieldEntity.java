package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class FieldEntity {
    private UUID uuid;
    private String name;
    private UUID baUuid;
    private UUID areaUuid;
    private UUID typeUuid;
    private String licenses;
    private Boolean esg;
    private Integer yearOpen;
    private UUID productSaleUuid;
}
