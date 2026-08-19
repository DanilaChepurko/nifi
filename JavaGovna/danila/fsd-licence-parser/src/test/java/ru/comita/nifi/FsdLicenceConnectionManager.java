package ru.comita.nifi;

import java.util.List;

public class FsdLicenceConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("field", "pool", "area", "business_associate", "horizon", "horizon_document",
                "horizon_document_dop_info", "subsoil_area");
    }

}
