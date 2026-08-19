package ru.comita.nifi;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class FSDExcelLoaderServiceTest {
    protected ConnectionManager connectionManager;
    private final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .toFormatter();

    protected Map<String, String> getDictionaryMap(Connection connection, String tableName) throws SQLException {
        return getDictionaryMap(connection, "name", tableName);
    }

    protected Map<String, String> getDictionaryMap(Connection connection, String columnName, String tableName) throws SQLException {
        String sql = String.format("select %s,uuid from %s", columnName, tableName);
        Statement resultStatement = connection.createStatement();
        ResultSet resultSet = resultStatement.executeQuery(sql);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, String> resultMap = new HashMap<>();
        while (resultSet.next()) {
            int nameIndex = 0;
            int uuidIndex = 0;
            for (int i = 1; i <= columnCount; i++) {
                String metaColumnName = metaData.getColumnName(i);
                if (metaColumnName.equals(columnName)) {
                    nameIndex = i;
                } else if (metaColumnName.equals("uuid")) {
                    uuidIndex = i;
                }
            }
            resultMap.put(resultSet.getString(nameIndex), resultSet.getString(uuidIndex));
        }
        return resultMap;
    }

    protected List<Map<String, String>> getResultMap(Connection connection, String tableName) throws SQLException {
        String sql = "select * from " + tableName;
        Statement resultStatement = connection.createStatement();
        ResultSet resultSet = resultStatement.executeQuery(sql);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, String>> results = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, String> resultMap = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                resultMap.put(metaData.getColumnName(i), resultSet.getString(i));
            }
            results.add(resultMap);
        }
        return results;
    }

    protected boolean assertTimeAndValue(Map<String, String> result, String timeTypeUUID, Integer month, Integer year, String value) {
        if ((result.get("period_type_uuid") + result.get("start_date")).equals(timeTypeUUID + dateToString(month, year))) {
            assertEquals(new BigDecimal(value).toPlainString(), new BigDecimal(result.get("value")).stripTrailingZeros().toPlainString());
            return true;
        }
        return false;
    }

    protected String dateToString(Integer month, Integer year) {
        return dateToString(1, month, year);
    }

    protected String dateToString(Integer day, Integer month, Integer year) {
        return LocalDate.of(year, month, day)
                .atStartOfDay()
                .format(dateTimeFormatter);
    }

    protected void assertBigDecimal(String expected, String actual) {
        assertEquals(new BigDecimal(expected).toPlainString(), new BigDecimal(actual)
                .stripTrailingZeros().toPlainString());
    }

}
