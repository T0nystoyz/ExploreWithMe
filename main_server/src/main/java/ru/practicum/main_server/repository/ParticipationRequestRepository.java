package ru.practicum.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.ParticipationRequest;
import ru.practicum.main_server.model.User;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequester(User requester);

    ParticipationRequest findByEventAndRequester(Event event, User requester);

    List<ParticipationRequest> findAllByEventId(long eventId);

    ParticipationRequest getReferenceById(Long requestId);
}
