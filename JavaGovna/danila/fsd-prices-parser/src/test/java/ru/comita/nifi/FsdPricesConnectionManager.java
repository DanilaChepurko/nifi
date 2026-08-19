package ru.comita.nifi;

import java.util.List;

public class FsdPricesConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("r_analytic", "field", "business_associate", "r_scenario", "header_econom", "gdo_prices");
    }

}
