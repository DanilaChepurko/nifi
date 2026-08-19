package ru.comita.lib.exception;

public class ExcelParserException extends GroupedException {

    public ExcelParserException(String message, String columnAddress, Integer rowNum) {
        super(String.format("Колонка: %s, строка: %s, ошибка: %s", columnAddress, rowNum, message));
    }

    public ExcelParserException(String message, String cellAddress) {
        super(String.format("Ячейка: %s, ошибка: %s", cellAddress, message));
    }
}
