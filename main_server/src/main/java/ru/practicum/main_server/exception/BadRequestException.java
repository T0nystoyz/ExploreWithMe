package ru.practicum.main_server.exception;

/**
 * Исключение для 400 ошибки
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
