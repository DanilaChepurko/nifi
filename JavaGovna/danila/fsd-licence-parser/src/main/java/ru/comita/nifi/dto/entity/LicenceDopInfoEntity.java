package ru.comita.nifi.dto.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class LicenceDopInfoEntity {
    private UUID horizonDocumentUuid;
    private Integer devStartYear;
    private Integer turon1PctYear;
    private Integer oilLicenseYear;
    private Integer oilGeoLicenseYear;
    private Integer oil1PctYear;
    private BigDecimal oilRecovery2011;
    private BigDecimal oilKkanLimit;
    private Integer oilKkanLastYear;
    private UUID subsoilAreaUuid;
}
