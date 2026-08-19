package ru.comita.nifi.dto.schema;

import lombok.Data;

@Data
public class ProjectEquipmentSchema {
    private Integer startRow;
    private String nameColumn;
    private String termBuColumn;
    private String termNuColumn;
    private String depricationPremiumColumn;
    private String termExpensesColumn;
    private String commissioningColumn;
}
