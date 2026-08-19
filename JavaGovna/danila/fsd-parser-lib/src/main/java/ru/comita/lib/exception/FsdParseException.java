package ru.comita.lib.exception;

public class FsdParseException extends RuntimeException {
    public FsdParseException(Throwable cause) {
        super(cause);
    }

    public FsdParseException(String message) {
        super(message);
    }
}
