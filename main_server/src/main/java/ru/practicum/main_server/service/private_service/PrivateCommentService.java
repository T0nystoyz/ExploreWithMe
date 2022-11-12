package ru.practicum.main_server.service.private_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.exception.BadRequestException;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.CommentMapper;
import ru.practicum.main_server.model.Comment;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.User;
import ru.practicum.main_server.model.dto.CommentDto;
import ru.practicum.main_server.repository.CommentRepository;
import ru.practicum.main_server.repository.EventRepository;
import ru.practicum.main_server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.main_server.model.CommentState.NEW;
import static ru.practicum.main_server.model.State.PUBLISHED;

@Slf4j
@Service
@Transactional
public class PrivateCommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public PrivateCommentService(CommentRepository commentRepository,
                                 UserRepository userRepository, EventRepository eventRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public CommentDto createComment(long userId, long eventId, CommentDto commentDto) {
        checkEventPublished(eventId);
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setEvent(getEventFromDbOrThrow(eventId));
        comment.setAuthor(getUserFromDbOrThrow(userId));
        comment.setState(NEW);
        comment.setCreated(LocalDateTime.now());
        log.info("комментарий создан {}.", commentDto.getText());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    public void deleteComment(long userId, long commentId) {
        checkAuthor(userId, commentId);
        commentRepository.deleteById(commentId);
    }

    public CommentDto updateComment(long userId, long commentId, CommentDto commentDto) {
        checkAuthor(userId, commentId);
        commentDto.setState(String.valueOf(NEW));
        Comment updatedComment = CommentMapper.toComment(commentDto);
        return CommentMapper.toCommentDto(commentRepository.save(updatedComment));
    }

    public List<CommentDto> readEventComments(Long eventId){
        return commentRepository.findByEventId(eventId).stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

    private void checkAuthor(long userId, long commentId) {
        if (getCommentFromDbOrThrow(commentId).getAuthor().getId() != userId) {
            throw new BadRequestException(String.format("PrivateCommentService: только автор или администратор могут " +
                    "редактировать/удалять комментарий. commentId=%d, userId=%d", commentId, userId));
        }
    }

    private Comment getCommentFromDbOrThrow(long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                String.format("PrivateCommentService: комментария по id=%d нет в базе", commentId)));
    }

    private Event getEventFromDbOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("PrivateCommentService: события по id=%d нет в базе", id)));
    }

    private void checkEventPublished(long eventId) {
        if (!eventRepository.getReferenceById(eventId).getState().equals(PUBLISHED)) {
            throw new BadRequestException("Нельзя комментировать неопубликованное событие");
        }
    }

    private User getUserFromDbOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("PrivateCommentService: пользователя по id=%d нет в базе", id)));
    }
}

