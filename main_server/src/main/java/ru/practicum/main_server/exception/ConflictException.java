package ru.practicum.main_server.exception;

/**
 * Исключение для ошибки 409
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
