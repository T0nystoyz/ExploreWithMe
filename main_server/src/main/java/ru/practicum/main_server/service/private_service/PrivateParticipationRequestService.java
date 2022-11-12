package ru.practicum.main_server.service.private_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.exception.BadRequestException;
import ru.practicum.main_server.exception.ForbiddenException;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.ParticipationRequestMapper;
import ru.practicum.main_server.model.*;
import ru.practicum.main_server.model.dto.ParticipationRequestDto;
import ru.practicum.main_server.repository.EventRepository;
import ru.practicum.main_server.repository.ParticipationRequestRepository;
import ru.practicum.main_server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PrivateParticipationRequestService {
    private final ParticipationRequestRepository participationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public PrivateParticipationRequestService(ParticipationRequestRepository participationRepository,
                                              UserRepository userRepository, EventRepository eventRepository) {
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public List<ParticipationRequestDto> readParticipationRequests(Long userId) {
        log.info("PrivateParticipationRequestService: чтение запросов отправленных пользователем с id={}", userId);
        return participationRepository.findAllByRequesterId(userId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        User requester = getUserFromDbOrThrow(userId);
        Event event = getEventFromDbOrThrow(eventId);
        validateRequest(requester, event);
        ParticipationRequest participation = ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status(Status.CONFIRMED)
                .build();
        if (event.isRequestModeration()) {
            participation.setStatus(Status.PENDING);
        }
        log.info("PrivateParticipationRequestService: создание запроса пользователем с id={} на событие с id={}",
                userId, eventId);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRepository.save(participation));
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest participation = getRequestFromDbOrThrow(requestId);
        if (userId.equals(participation.getRequester().getId())) {
            participation.setStatus(Status.CANCELED);
        } else {
            throw new ForbiddenException("только инициатор события может его отменить");
        }
        return ParticipationRequestMapper.toParticipationRequestDto(participationRepository.save(participation));
    }

    public List<ParticipationRequestDto> readEventParticipation(Long userId, Long eventId) {
        validateInitiator(userId, eventId);
        return participationRepository.findAllByEventId(eventId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto approveParticipationRequest(Long userId, Long eventId, Long requestId) {
        validateInitiator(userId, eventId);
        Event event = getEventFromDbOrThrow(eventId);
        ParticipationRequest participation = getRequestFromDbOrThrow(requestId);
        if (!participation.getStatus().equals(Status.PENDING)) {
            throw new ForbiddenException("чтобы принять запрос, он должен быть в статусе PENDING");
        }
        if (event.getConfirmedRequests() != null && event.getParticipantLimit() <= event.getConfirmedRequests()) {
            participation.setStatus(Status.REJECTED);
        }
        participation.setStatus(Status.CONFIRMED);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRepository.save(participation));
    }

    @Transactional
    public ParticipationRequestDto rejectParticipationRequest(Long userId, Long eventId, Long requestId) {
        validateInitiator(userId, eventId);
        ParticipationRequest participation = getRequestFromDbOrThrow(requestId);
        participation.setStatus(Status.REJECTED);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRepository.save(participation));
    }

    private void validateInitiator(Long userId, Long eventId) {
        Event event = getEventFromDbOrThrow(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("только инициатор события может просматривать запросы на участие");
        }
    }

    private void validateRequest(User requester, Event event) {
        if (participationRepository.findByEventAndRequester(event, requester) != null) {
            throw new BadRequestException(String.format("запрос пользователя с id=%d на участие в событии с " +
                    "id=%d уже существует", event.getId(), requester.getId()));
        }
        if (event.getInitiator().equals(requester)) {
            throw new ForbiddenException(String.format("инициатор с id=%d события с id=%d не может " +
                    "создать на него запрос", requester.getId(), event.getId()));
        }
        if (!(event.getState().equals(State.PUBLISHED))) {
            throw new ForbiddenException("невозможно создать запрос на неопубликованное событие");
        }
        if (event.getParticipantLimit() != null && event.getConfirmedRequests() != null
                && event.getParticipantLimit() != 0 && event.getParticipantLimit() <= event.getConfirmedRequests()) {
            throw new ForbiddenException(String.format("превышено количество участников события - %d",
                    event.getConfirmedRequests()));
        }
    }

    private Event getEventFromDbOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("AdminCompilationService: события по id=%d нет в базе", id)));
    }

    private ParticipationRequest getRequestFromDbOrThrow(Long requestId) {
        return participationRepository.findById(requestId).orElseThrow(() -> new NotFoundException(
                String.format("PrivateParticipationRequestService: запроса по id=%d нет в базе", requestId)));
    }

    private User getUserFromDbOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("PrivateEventService: пользователя по id=%d нет в базе", id)));
    }
}