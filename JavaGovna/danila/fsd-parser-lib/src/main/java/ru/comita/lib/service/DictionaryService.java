package ru.comita.lib.service;

import ru.comita.lib.exception.FsdParseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
public class DictionaryService {

    private final Connection connection;
    private final Map<String, Map<String, UUID>> cache = new HashMap<>();

    public DictionaryService(Connection connection) {
        this.connection = connection;
    }

    public UUID getUUIDByName(String tableName, String nameColumn, String name) {
        name = name.trim();
        UUID uuid = getFromCache(tableName, name);
        if (uuid != null) {
            return uuid;
        }
        String sql = String.format("SELECT uuid FROM %s where UPPER(%s) = UPPER('%s')", tableName, nameColumn, name);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                UUID resultUUID = UUID.fromString(resultSet.getString("uuid"));
                addToCache(tableName, name, resultUUID);
                return resultUUID;
            }
        } catch (Exception e) {
            throw new FsdParseException(e);
        }
        return null;
    }

    public UUID getUUIDByThreeFields(String tableName,
                                     String column1, Object value1,
                                     String column2, Object value2,
                                     String column3, Object value3) {
        if (value1 == null || value2 == null || value3 == null) {
            return null;
        }

        String combinedKey = String.join("|",
                value1.toString(), value2.toString(), value3.toString());

        UUID cached = getFromCache(tableName, combinedKey);
        if (cached != null) {
            return cached;
        }

        String sql = String.format("SELECT uuid FROM %s WHERE %s = ? AND %s = ? AND %s = ? LIMIT 1",
                tableName, column1, column2, column3);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameter(ps, 1, value1);
            setParameter(ps, 2, value2);
            setParameter(ps, 3, value3);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID result = (UUID) rs.getObject("uuid");
                    addToCache(tableName, combinedKey, result);
                    return result;
                }
            }
        } catch (Exception e) {
            throw new FsdParseException("Error querying " + tableName +
                    " with params: " + column1 + "=" + value1 +
                    ", " + column2 + "=" + value2 +
                    ", " + column3 + "=" + value3 +
                    ". Cause: " + e.getMessage());
        }
        return null;
    }

    private void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NULL);
        } else if (value instanceof UUID) {
            ps.setObject(index, value, Types.OTHER);
        } else if (value instanceof LocalDate) {
            ps.setDate(index, Date.valueOf((LocalDate) value));
        } else if (value instanceof String) {
            ps.setString(index, ((String) value).trim());
        } else {
            ps.setObject(index, value);
        }
    }


    private UUID getFromCache(String tableName, String name) {
        Map<String, UUID> tableCache = cache.get(tableName);
        if (tableCache != null) {
            return tableCache.get(name);
        }
        return null;
    }

    private void addToCache(String tableName, String name, UUID uuid) {
        Map<String, UUID> tableCache = cache.get(tableName);
        if (tableCache == null) {
            tableCache = new HashMap<>();
        }
        tableCache.put(name, uuid);
        cache.put(tableName, tableCache);
    }
}
