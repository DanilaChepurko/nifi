package ru.comita.nifi.dto.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HeaderEconomEntity {
    private UUID uuid;
    private UUID scenarioUuid;
    private Integer year;
    private String fsdSource;
    private LocalDateTime createdDate;
}
