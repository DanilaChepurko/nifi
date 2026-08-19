package ru.comita.nifi;

import java.util.List;

public class FsdProjectEventConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of( "project_event","project_step","horizon", "business_associate");
    }

}
