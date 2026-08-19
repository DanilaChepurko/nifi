package ru.comita.nifi;

import java.util.List;

public class FsdProjectEconConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("r_analytic",  "business_associate", "r_scenario", "header_econom", "project_step", "project_step_econ");
    }

}
