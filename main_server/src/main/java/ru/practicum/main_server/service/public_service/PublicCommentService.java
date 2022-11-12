package ru.practicum.main_server.service.public_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.mapper.CommentMapper;
import ru.practicum.main_server.model.dto.CommentDto;
import ru.practicum.main_server.repository.CommentRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.main_server.model.CommentState.APPROVED;

@Slf4j
@Service
@Transactional
public class PublicCommentService {
    private final CommentRepository commentRepository;


    @Autowired
    public PublicCommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<CommentDto> readEventComments(Long eventId) {
        return commentRepository.findByStateAndEventId(APPROVED, eventId).stream().map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
