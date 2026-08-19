package ru.comita.lib.exception;

public class ResourceNotFoundException extends ResourceException {

    public ResourceNotFoundException() {
        super("Не найдена одна из схем");

    }
}
