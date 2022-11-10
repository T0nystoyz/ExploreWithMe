package ru.practicum.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.ParticipationRequest;
import ru.practicum.main_server.model.Status;
import ru.practicum.main_server.model.User;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long requester);

    ParticipationRequest findByEventAndRequester(Event event, User requester);

    List<ParticipationRequest> findAllByEventId(long eventId);

    ParticipationRequest getReferenceById(Long requestId);

    @Query("select r from Request as r where r.status = ?1 and (r.event in :events)")
    List<ParticipationRequest> findByStatusAndEvents(Status status, List<Event> events);

    Long countByEventIdAndStatus(Long eventId, Status status);
}
