package ru.comita.lib.exception;

public class DictionaryException extends GroupedException {

    public DictionaryException(String tableName, String value) {
        super(String.format("Отсутствует справочник в таблице %s для значения %s", tableName, value));
    }
}
