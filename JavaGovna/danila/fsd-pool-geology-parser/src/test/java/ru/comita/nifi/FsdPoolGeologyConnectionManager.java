package ru.comita.nifi;

import java.util.List;

public class FsdPoolGeologyConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("field", "pool_geology", "business_associate", "horizon", "pool", "area");
    }

}
