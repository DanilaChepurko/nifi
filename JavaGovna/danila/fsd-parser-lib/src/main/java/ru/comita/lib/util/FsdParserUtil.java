package ru.comita.lib.util;

import com.github.pjfanning.xlsx.StreamingReader;
import com.github.pjfanning.xlsx.impl.StreamingWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import ru.comita.lib.dto.TimeType;
import ru.comita.lib.exception.DateParseException;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static ru.comita.lib.dto.TimeType.month;
import static ru.comita.lib.dto.TimeType.quarter;
import static ru.comita.lib.dto.TimeType.year;

public class FsdParserUtil {

    private static final List<String> months = List.of("январь", "февраль", "март", "апрель", "май", "июнь",
            "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь");

    public static StreamingWorkbook getWorkbookFromFile(File file) {
        return (StreamingWorkbook) StreamingReader.builder()
                .rowCacheSize(100)
                .bufferSize(4096)
                .open(file);
    }

    public static Boolean isInSameRow(Row row, String address) {
        if (address == null) {
            return false;
        }
        CellReference cellReference = new CellReference(address);
        return row.getRowNum() == cellReference.getRow();
    }

    public static BigDecimal getBigDecimalValueFromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return BigDecimal.valueOf(Double.parseDouble(value));
    }


    public static LocalDateTime getTime(Integer year, TimeType periodType, String period) {
        if (period == null || period.isEmpty()) {
            return LocalDate.of(year, 1, 1).atStartOfDay();
        }
        period = period.toLowerCase().trim();
        if (periodType.equals(quarter)) {
            switch (period) {
                case "1 кв.":
                    return fromYearAndMonth(year, Month.JANUARY);
                case "2 кв.":
                    return fromYearAndMonth(year, Month.APRIL);
                case "3 кв.":
                    return fromYearAndMonth(year, Month.JULY);
                case "4 кв.":
                    return fromYearAndMonth(year, Month.OCTOBER);
                default:
                    throw new DateParseException("Не удалось распарсить квартал: " + period);
            }
        } else {
            switch (period) {
                case "январь":
                    return fromYearAndMonth(year, Month.JANUARY);
                case "февраль":
                    return fromYearAndMonth(year, Month.FEBRUARY);
                case "март":
                    return fromYearAndMonth(year, Month.MARCH);
                case "апрель":
                    return fromYearAndMonth(year, Month.APRIL);
                case "май":
                    return fromYearAndMonth(year, Month.MAY);
                case "июнь":
                    return fromYearAndMonth(year, Month.JUNE);
                case "июль":
                    return fromYearAndMonth(year, Month.JULY);
                case "август":
                    return fromYearAndMonth(year, Month.AUGUST);
                case "сентябрь":
                    return fromYearAndMonth(year, Month.SEPTEMBER);
                case "октябрь":
                    return fromYearAndMonth(year, Month.OCTOBER);
                case "ноябрь":
                    return fromYearAndMonth(year, Month.NOVEMBER);
                case "декабрь":
                    return fromYearAndMonth(year, Month.DECEMBER);
                default:
                    throw new DateParseException("Не удалось распарсить месяц: " + period);
            }
        }
    }

    private static LocalDateTime fromYearAndMonth(Integer year, Month month) {
        return LocalDate.of(year, month, 1).atStartOfDay();
    }

    public static LocalDateTime fromDateString(String dateString) {
        if (dateString == null) {
            return null;
        }
        if (dateString.contains("кв")) {
            dateString = dateString.replace("г.", "").stripTrailing();
            String year = dateString.substring(dateString.length() - 4);
            Month month = getMonthByQuarter(dateString);
            return LocalDate.of(Integer.parseInt(year), month.getValue(), 1).atStartOfDay();
        } else {
            Integer year = parseStringToInt(dateString);
            return LocalDate.of(year, 1, 1).atStartOfDay();
        }
    }

    private static Month getMonthByQuarter(String dateString) {
        Month month;
        if (dateString.contains("1 кв") || dateString.startsWith("I кв")) {
            month = Month.JANUARY;
        } else if (dateString.contains("2 кв") || dateString.startsWith("II кв")) {
            month = Month.APRIL;
        } else if (dateString.contains("3 кв") || dateString.startsWith("III кв")) {
            month = Month.JULY;
        } else if (dateString.contains("4 кв") || dateString.contains("IV кв")) {
            month = Month.OCTOBER;
        } else {
            throw new DateParseException("Не удалось определить квартрал по значению " + dateString);
        }
        return month;
    }

    public static Integer parseStringToInt(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        return Integer.parseInt(roundString(string));
    }

    public static TimeType getTimeType(String timeTypeValue) {
        if (timeTypeValue == null || timeTypeValue.equalsIgnoreCase("год")) {
            return year;
        }
        timeTypeValue = timeTypeValue.toLowerCase().trim();
        if (months.contains(timeTypeValue)) {
            return month;
        }
        if (timeTypeValue.contains("кв")) {
            return quarter;
        } else {
            try {
                parseStringToInt(timeTypeValue);
                return year;
            } catch (NumberFormatException ignored) {
            }
        }
        throw new DateParseException(String.format("Не удалось определить time_type по значению %s.", timeTypeValue));
    }

    public static Month getMonthForQuarterByColumnNumber(int colNum) {
        Month month = null;
        if (colNum == 0) {
            month = Month.JANUARY;
        } else if (colNum == 1) {
            month = Month.APRIL;
        } else if (colNum == 2) {
            month = Month.JULY;
        } else if (colNum == 3) {
            month = Month.OCTOBER;
        }
        return month;
    }

    public static String roundString(String string) {
        if (string.contains(".")) {
            return string.substring(0, string.indexOf(".")).trim();
        } else {
            return string.trim();
        }
    }

    public static void setBigDecimalStatementValue(PreparedStatement preparedStatement,
                                                   BigDecimal value,
                                                   Integer index) throws SQLException {
        setBigDecimalStatementValue(preparedStatement, value, index, 3);

    }

    public static void setBigDecimalStatementValue(PreparedStatement preparedStatement,
                                                   BigDecimal value,
                                                   Integer index,
                                                   Integer scale) throws SQLException {
        if (value == null) {
            preparedStatement.setObject(index, null);
        } else {
            preparedStatement.setBigDecimal(index, value
                    .setScale(scale, RoundingMode.HALF_UP)
                    .stripTrailingZeros());
        }
    }

}
