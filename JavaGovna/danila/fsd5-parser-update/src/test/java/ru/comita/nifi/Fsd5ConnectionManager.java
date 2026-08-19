package ru.comita.nifi;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Fsd5ConnectionManager extends ConnectionManager {
    // @Override
    // public void clearAllTables(Connection connection) {
    //     try {
    //         connection.setAutoCommit(false);
    //         Statement statement = connection.createStatement();
    //         List<String> tableNames = getTableNames();
    //         if (tableNames != null) {
    //             for (String tableName : tableNames) {
    //                 // Используем IF EXISTS + CASCADE для безопасного удаления
    //                 statement.addBatch("DROP TABLE IF EXISTS " + tableName + " CASCADE");
    //             }
    //         }
    //         statement.executeBatch();
    //         connection.commit();
    //     } catch (SQLException e) {
    //         try {
    //             connection.rollback();
    //         } catch (SQLException ex) {
    //             throw new RuntimeException("Failed to rollback after error", ex);
    //         }
    //         throw new RuntimeException("Failed to clear tables", e);
    //     } 
    // }


    
    @Override
    protected List<String> getTableNames() {
        return List.of(
    "field",
    "r_analytic",
    "horizon",
    "r_scenario",
    "r_version",
    "project_step",
    "r_period_type",
    "horizon_area",
    "business_associate",
    "header_development",
    "header_development_meta_inf",
    "pden_vol_summary_development",
    "project_event"
);
    }

}
