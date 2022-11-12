package ru.practicum.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.main_server.model.Comment;
import ru.practicum.main_server.model.CommentState;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c where c.state = ?1 and c.event.id = ?2")
    List<Comment> findByStateAndEventId(CommentState state, Long eventId);

    List<Comment> findByEventId(Long eventId);
}
