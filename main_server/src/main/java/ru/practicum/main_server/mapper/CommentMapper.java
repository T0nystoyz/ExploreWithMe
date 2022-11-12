package ru.practicum.main_server.mapper;

import ru.practicum.main_server.model.Comment;
import ru.practicum.main_server.model.CommentState;
import ru.practicum.main_server.model.dto.CommentDto;

import java.time.LocalDateTime;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setText(comment.getText());
        commentDto.setCreated(comment.getCreated());
        commentDto.setState(commentDto.getState());
        return commentDto;
    }

    public static Comment toComment(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setText(commentDto.getText());
        comment.setCreated(LocalDateTime.now());
        comment.setState(CommentState.valueOf(commentDto.getState()));
        return comment;
    }

}
