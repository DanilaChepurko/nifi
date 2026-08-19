package ru.comita.nifi;

import java.util.List;

public class FsdProjectStepConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("invest_program", "horizon_area", "horizon",
                "field");
    }

}
