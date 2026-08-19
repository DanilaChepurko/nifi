package ru.comita.nifi;

import java.util.List;

public class FsdProjectEventEquipmentConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("business_associate","field","r_version","header_econom", "project_event","equipment","equipment_in_use");
    }

}
