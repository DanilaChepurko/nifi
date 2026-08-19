package ru.comita.lib.exception;

public class ResourceParseException extends ResourceException {

    public ResourceParseException(Throwable cause) {
        super("Ошибка при парсинге схемы: ".concat(cause.getLocalizedMessage()), cause);
    }
}
