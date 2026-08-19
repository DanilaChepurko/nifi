package ru.comita.lib.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import ru.comita.lib.dto.TimeType;
import ru.comita.lib.exception.DictionaryException;
import ru.comita.lib.exception.ExcelParserException;
import ru.comita.lib.util.FsdParserUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

public class ExcelParserService {

    private final DictionaryService dictionaryService;

    public ExcelParserService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public UUID getUUIDByCellAddress(Row row, String tableName, String address) {
        return getUUIDByCellAddress(row, tableName, "name", address);
    }

    public UUID getUUIDByCellAddress(Row row, String tableName, String nameColumn, String address) {
        if (address == null) {
            return null;
        }
        String cellValue = getCellValueByAddress(row, address);
        if (cellValue == null || cellValue.isEmpty()) {
            return null;
        }

        return getUUIDByValue(tableName, nameColumn, cellValue);
    }

    public UUID getUUIDByValue(String tableName, String nameValue) {
        return getUUIDByValue(tableName, "name", nameValue);
    }

    private UUID getUUIDByValue(String tableName, String nameColumn, String nameValue) {
        if (nameValue == null) {
            return null;
        }
        UUID result = dictionaryService.getUUIDByName(tableName, nameColumn, nameValue);
        if (result == null) {
            throw new DictionaryException(tableName, nameValue);
        }
        return result;
    }
    public UUID getUUIDByThreeCellValues(Row row, String tableName,
                                         String column1, String address1,
                                         String column2, String address2,
                                         String column3, String address3) {
        String value1 = getCellValueByAddress(row, address1);
        String value2 = getCellValueByAddress(row, address2);
        String value3 = getCellValueByAddress(row, address3);

        if (value1 == null || value2 == null || value3 == null) {
            return null;
        }

        return dictionaryService.getUUIDByThreeFields(tableName,
                column1, value1,
                column2, value2,
                column3, value3);
    }
    public UUID getUUIDByThreeParmValues(String tableName,
                                         String column1, Object value1,
                                         String column2, Object value2,
                                         String column3, Object value3) {
        if (value1 == null || value2 == null || value3 == null) {
            return null;
        }

        // Преобразуем параметры к нужным типам
        Object param1 = convertParameter(value1);
        Object param2 = convertParameter(value2);
        Object param3 = convertParameter(value3);

        return dictionaryService.getUUIDByThreeFields(tableName,
                column1, param1,
                column2, param2,
                column3, param3);
    }

    private Object convertParameter(Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            try {
                // Пробуем преобразовать строку в UUID
                return UUID.fromString(strValue);
            } catch (IllegalArgumentException e) {
                // Если не UUID, оставляем как строку
                return strValue.trim();
            }
        }
        return value;
    }
    public String getCellValueByAddress(Row row, String cellAddress) {
        Cell cell = getCell(row, cellAddress);
        if (cell == null) {
            return null;
        }
        CellType cellType = cell.getCellType();
        String value;
        if (cellType.equals(CellType.NUMERIC)) {
            try {
                value = String.valueOf(cell.getNumericCellValue());
            } catch (Exception ignored) {
                value = cell.getStringCellValue();
            }
        } else if (cellType.equals(CellType.ERROR)) {
            return null;
        } else {
            try {
                value = cell.getStringCellValue();
                if (value.equals("ERROR:  #N/A")) {
                    return null;
                }
                // Добавляем проверку на #ДЕЛ/0! в строковом представлении
                if (value.contains("#ДЕЛ/0!") || value.contains("#DIV/0!")) {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }
        if (value.isEmpty()) {
            return null;
        } else {
            return value.trim();
        }
    }

    public String getCellValueByAddress(Row row, Integer columnNum) {
        String analyticsColumn = CellReference.convertNumToColString(columnNum);
        return getCellValueByAddress(row, analyticsColumn);
    }

    public LocalDate getDateCellValue(Row row, String cellAddress) {
        Cell cell = getCell(row, cellAddress);
        if (cell == null) {
            return null;
        }
        Date dateCellValue;
        try {
            dateCellValue = cell.getDateCellValue();
            if (dateCellValue == null) {
                return null;
            }
            try {
                return new java.sql.Date(dateCellValue.getTime()).toLocalDate();
            } catch (Exception e) {
                return null;
            }
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public Cell getCell(Row row, String cellAddress) {
        if (cellAddress == null) {
            return null;
        }
        CellReference cellReference = new CellReference(cellAddress);
        if (row == null) {
            return null;
        }

        return row.getCell(cellReference.getCol());
    }

    public BigDecimal getBigDecimalValue(Row row, Integer columnNum) {
        String analyticsColumn = CellReference.convertNumToColString(columnNum);
        return getBigDecimalValue(row, analyticsColumn);
    }

    public BigDecimal getBigDecimalValue(Row row, String columnName) {
        String value = getCellValueByAddress(row, columnName);
        try {
            return FsdParserUtil.getBigDecimalValueFromString(value);
        } catch (NumberFormatException e) {
            throw new ExcelParserException(e.getLocalizedMessage(), columnName, row.getRowNum());
        }
    }

    public UUID getTimeTypeUUID(TimeType timeType) {
        if (timeType == null) {
            return null;
        }

        return getUUIDByValue("r_period_type", timeType.name());
    }

}
