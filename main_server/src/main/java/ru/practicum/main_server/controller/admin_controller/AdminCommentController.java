package ru.practicum.main_server.controller.admin_controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.model.CommentState;
import ru.practicum.main_server.model.dto.CommentDto;
import ru.practicum.main_server.service.admin_service.AdminCommentService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/comments")
@Slf4j
public class AdminCommentController {
    private final AdminCommentService commentService;

    public AdminCommentController(AdminCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentDto> readComments(@RequestParam CommentState state,
                                         @RequestParam long eventId){
        return commentService.readComments(state, eventId);
    }

    @PatchMapping("/{comId}/reject")
    public CommentDto reject(@PathVariable Long comId) {
        log.info(":::PATCH /admin/comments/{}/reject отклоненеие комментария", comId);
        return commentService.rejectComment(comId);
    }

    @PatchMapping("/{comId}/approve")
    public CommentDto approve(@PathVariable Long comId) {
        log.info(":::PATCH /admin/comments/{}/approve одобрение комментария", comId);
        return commentService.approveComment(comId);
    }

    @DeleteMapping("/{comId}")
    public void delete(@PathVariable Long comId) {
        log.info(":::DELETE /admin/comments/{} удаление комментария по id", comId);
        commentService.deleteComment(comId);
    }
}
