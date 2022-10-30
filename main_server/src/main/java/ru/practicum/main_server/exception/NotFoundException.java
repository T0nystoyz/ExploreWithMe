package ru.practicum.main_server.exception;

/**
 * Исключение для ошибки 404
 */
public class NotFoundException extends RuntimeException{
    public NotFoundException(String message) {
        super(message);
    }
}
