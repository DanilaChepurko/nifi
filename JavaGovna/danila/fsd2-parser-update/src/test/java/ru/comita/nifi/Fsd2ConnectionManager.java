package ru.comita.nifi;

import java.util.List;

public class Fsd2ConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("pden_vol_summary_project","equipment", "pden_option","horizon", "header_project", "r_analytic",
                "pool", "r_scenario", "facility", "project_step", "r_period_type", "r_version", "r_development_method",
                "business_associate");
    }

}
