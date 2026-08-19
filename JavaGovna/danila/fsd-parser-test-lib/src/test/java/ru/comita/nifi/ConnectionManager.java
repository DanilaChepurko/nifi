package ru.comita.nifi;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public abstract class ConnectionManager {

    private static Connection connection;
    private static EmbeddedPostgres postgres;
    private static boolean tablesCreated;

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String testcontainersEnabled = System.getenv("TESTCONTAINERS_ENABLED");
                if (testcontainersEnabled != null) {
                    DriverManager.registerDriver(new org.postgresql.Driver());
                    connection = DriverManager.getConnection("jdbc:postgresql://host.docker.internal:5411/postgres", "postgres", "postgres");
                } else {
                    if (postgres == null) {
                        postgres = EmbeddedPostgres.builder().start();
                    }
                    connection = postgres.getPostgresDatabase().getConnection();
                }
            }

            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initTables(Connection connection) {
        if (tablesCreated) {
            return;
        }
        try (InputStream initSqlIn = ConnectionManager.class.getClassLoader()
                .getResourceAsStream("init.sql")) {
            assert initSqlIn != null;

            String initSql = new String(initSqlIn.readAllBytes(), StandardCharsets.UTF_8);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            Arrays.stream(initSql.split(";")).forEach(sql ->
            {
                try {
                    statement.addBatch(sql);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            statement.executeBatch();
            connection.commit();
            tablesCreated = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void clearAllTables(Connection connection) {
        try {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            List<String> tableNames = getTableNames();
            if (tableNames != null) {
                tableNames.forEach(tableName -> {
                    try {
                        statement.addBatch("drop table " + tableName);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            statement.executeBatch();
            connection.commit();
            tablesCreated = false;
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract List<String> getTableNames();

    public void clearAllTables() {
        clearAllTables(connection);
    }
}
