package ru.comita.nifi;

import java.util.List;

public class FsdTaxesConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("header_econom", "tax_indicator", "r_analytic", "r_scenario");
    }

}
