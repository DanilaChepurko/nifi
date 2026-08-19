package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class LicenceSchema {
    private Integer headerStartRow;
    private Integer tableStartRow;
    private String field;
    private String ba;
    private String horizonColumn;
    private String poolColumn;
    private String licenceCodeColumn;
    private String licenceYearColumn;
    private String devStartYearColumn;
    private String turon1PctYearColumn;
    private String oilLicenseYearColumn;
    private String oilGeoLicenseYearColumn;
    private String oil1PctYearColumn;
    private String oilRecovery2011Column;
    private String oilKkanLimitColumn;
    private String oilKkanLastYearColumn;
    private String subsoilAreaUuidColumn;
}