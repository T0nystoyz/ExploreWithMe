package ru.practicum.main_server.exception;

/**
 * Исключение для ошибки 403
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
