package ru.comita.nifi;

import java.util.List;

public class FsdMacroConnectionManager extends ConnectionManager {

    @Override
    protected List<String> getTableNames() {
        return List.of("macro_tax","header_econom", "r_analytic", "r_scenario");
    }

}
