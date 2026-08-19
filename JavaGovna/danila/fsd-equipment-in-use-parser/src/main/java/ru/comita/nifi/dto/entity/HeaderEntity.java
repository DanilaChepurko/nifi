package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HeaderEntity {
    private UUID uuid;
    private UUID baUuid;
    private UUID fieldUuid;
    private UUID horizonUuid;
    private Integer year;
    private UUID version;
    private Integer iteration;
    private String name;
    private LocalDateTime createdDate;
    private UUID scenario;
    private String fsdSource;
}
