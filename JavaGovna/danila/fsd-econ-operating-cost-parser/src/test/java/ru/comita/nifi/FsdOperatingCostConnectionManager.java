package ru.comita.nifi;

import java.util.List;

public class FsdOperatingCostConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("header_econom", "horizon_econ_param", "r_analytic",
                "horizon", "horizon_fond", "field", "business_associate");
    }
}
