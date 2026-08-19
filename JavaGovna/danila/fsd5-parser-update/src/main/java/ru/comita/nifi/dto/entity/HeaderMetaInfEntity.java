package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class HeaderMetaInfEntity {
    private UUID headerUuid;
    private String author;
    private LocalDate fillInDate;
    private UUID syncScenario;
    private UUID syncVersion;
    private UUID syncComparedScenario;
    private UUID syncComparedVersion;
}
