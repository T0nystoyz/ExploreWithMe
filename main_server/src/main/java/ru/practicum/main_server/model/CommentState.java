package ru.practicum.main_server.model;
/**
 * Возможные статусы для Comment: NEW, APPROVED, REJECTED
 */
public enum CommentState {
    NEW, // опубликованный коментарий, не просмотренный администратором
    APPROVED, // подтвержденный администратором, такие комментарии могут видеть неавторизированные пользователи
    REJECTED // отклоненный администратором
}
