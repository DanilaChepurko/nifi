package ru.comita.nifi;

import java.util.List;

public class FsdProjectEquipmentConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of( "equipment");
    }

}
