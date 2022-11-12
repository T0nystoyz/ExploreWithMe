package ru.practicum.main_server.exception;
/**
 * Исключение для ошибки 500
 */
public class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException(String message) {
        super(message);
    }
}