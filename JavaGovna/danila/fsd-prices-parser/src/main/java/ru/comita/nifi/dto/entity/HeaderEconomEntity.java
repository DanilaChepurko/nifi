package ru.comita.nifi.dto.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HeaderEconomEntity {
    private UUID uuid;
    private UUID scenarioUuid;
    private UUID fieldUuid;
    private UUID baUuid;
    private String fsdSource;
    private Integer year;
    private LocalDateTime createdDate;
}
