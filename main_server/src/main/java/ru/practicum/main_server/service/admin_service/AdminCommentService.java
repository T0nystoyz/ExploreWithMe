package ru.practicum.main_server.service.admin_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.CommentMapper;
import ru.practicum.main_server.model.Comment;
import ru.practicum.main_server.model.CommentState;
import ru.practicum.main_server.model.dto.CommentDto;
import ru.practicum.main_server.repository.CommentRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.main_server.model.CommentState.APPROVED;
import static ru.practicum.main_server.model.CommentState.REJECTED;

@Slf4j
@Service
@Transactional
public class AdminCommentService {
    private final CommentRepository commentRepository;

    @Autowired
    public AdminCommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<CommentDto> readComments(CommentState state, long eventId) {
        return commentRepository.findByStateAndEventId(state, eventId).stream().map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public void deleteComment(long commentId) {
        commentRepository.delete(getCommentFromDbOrThrow(commentId));
    }

    public CommentDto rejectComment(long commentId) {
        Comment comment = getCommentFromDbOrThrow(commentId);
        comment.setState(REJECTED);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    public CommentDto approveComment(long commentId) {
        Comment comment = getCommentFromDbOrThrow(commentId);
        comment.setState(APPROVED);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private Comment getCommentFromDbOrThrow(long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                String.format("PrivateCommentService: комментария по id=%d нет в базе", commentId)));
    }
}