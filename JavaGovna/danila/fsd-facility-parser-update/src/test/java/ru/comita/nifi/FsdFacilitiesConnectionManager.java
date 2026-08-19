package ru.comita.nifi;

import java.util.List;

public class FsdFacilitiesConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("facility", "field", "horizon", "horizon_area");
    }

}
