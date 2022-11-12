package ru.practicum.main_server.controller.private_controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.model.dto.CommentDto;
import ru.practicum.main_server.service.private_service.PrivateCommentService;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users/{userId}/comments")
@Slf4j
public class PrivateCommentController {
    private final PrivateCommentService commentService;

    public PrivateCommentController(PrivateCommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public CommentDto createComment(@PathVariable Long userId,
                                    @RequestParam Long eventId,
                                    @RequestBody @Valid CommentDto commentDto) {
        log.info(":::POST /users/{}/comments создание комментария на событие c id={} ", userId, eventId);
        return commentService.createComment(userId, eventId, commentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @RequestBody CommentDto commentDto) {
        log.info(":::PATCH /users/{}/comments/{} редактирование коментария пользователя ", userId, commentId);
        return commentService.updateComment(userId, commentId, commentDto);
    }

    @DeleteMapping("/{commentId}")
    public void delete(@PathVariable Long commentId, @PathVariable Long userId) {
        log.info(":::DELETE /users/{}/comments/{} удаление комментария по id", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }
}
