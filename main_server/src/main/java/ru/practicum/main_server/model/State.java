package ru.practicum.main_server.model;

/**
 * Возможные состояния для Event: PENDING,PUBLISHED,CANCELED,CONFIRMED
 */
public enum State {
    PENDING,
    PUBLISHED,
    CANCELED,
    CONFIRMED
}
