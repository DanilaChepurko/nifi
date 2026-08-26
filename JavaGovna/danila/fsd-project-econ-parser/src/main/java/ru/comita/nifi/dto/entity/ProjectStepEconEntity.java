package ru.comita.nifi.dto.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProjectStepEconEntity {
    private UUID uuid;
    private UUID headerUuid;
    private UUID projectStepUuid;
    private String investProgram;
    private UUID scenario;
    private String constructionType;
    private String functionalGroup;
    private String complexReconstructionProgram;
    private String priority;
    private UUID projectStepDependency;
    private Integer pirStartYear;
    private Integer pirEndYear;
    private Integer smrStartYear;
    private Integer smrEndYear;
    private UUID analyticUuid;
    private Integer projectStepCompletionYear;
    private BigDecimal value;
}