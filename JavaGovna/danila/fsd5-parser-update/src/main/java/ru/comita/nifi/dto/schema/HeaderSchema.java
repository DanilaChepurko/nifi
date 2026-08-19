package ru.comita.nifi.dto.schema;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HeaderSchema {
    private String version;
    private String scenario;
    private String horizon;
    private String year;
    private String ba;
    private String fluidType;
    private String author;
    private String fillInDate;
    private String field;
    private String syncScenario;
    private String syncVersion;
    private String syncComparedScenario;
    private String syncComparedVersion;
    private Integer lastRow;
    private String dopScenario;

}
