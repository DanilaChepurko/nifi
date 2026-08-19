package ru.comita.nifi;

import java.util.List;

public class FsdFluidTypesConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("field", "fluid_types", "business_associate", "horizon");
    }

}
