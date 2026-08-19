package ru.comita.nifi;

import java.util.List;

public class FsdFieldConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("field", "r_field_type", "business_associate", "area");
    }

}
