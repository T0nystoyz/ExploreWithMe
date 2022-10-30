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
        checkUserInDb(userId);
        return participationRepository.findAllByRequester(userRepository.getReferenceById(userId))
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        checkEventInDb(eventId);
        checkUserInDb(userId);
        User requester = userRepository.getReferenceById(userId);
        Event event = eventRepository.getReferenceById(eventId);
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
        checkRequestInDb(requestId);
        ParticipationRequest participation = participationRepository.getReferenceById(requestId);
        if (userId.equals(participation.getRequester().getId())) {
            participation.setStatus(Status.CANCELED);
        } else {
            throw new ForbiddenException("только инициатор события может его отменить");
        }
        return ParticipationRequestMapper.toParticipationRequestDto(participationRepository.save(participation));
    }

    public List<ParticipationRequestDto> readEventParticipation(Long userId, Long eventId) {
        checkEventInDb(eventId);
        checkUserInDb(userId);
        checkInitiator(userId, eventId);
        return participationRepository.findAllByEventId(eventId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto approveParticipationRequest(Long userId, Long eventId, Long requestId) {
        checkEventInDb(eventId);
        checkInitiator(userId, eventId);
        checkUserInDb(userId);
        Event event = eventRepository.getReferenceById(eventId);
        ParticipationRequest participation = participationRepository.getReferenceById(requestId);
        if (!participation.getStatus().equals(Status.PENDING)) {
            throw new ForbiddenException("чтобы принять запрос, он должен быть в статусе PENDING");
        }
        Long countConfirmedRequests = participationRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        if (event.getParticipantLimit() >= countConfirmedRequests) {
            participation.setStatus(Status.REJECTED);
        }
        participation.setStatus(Status.CONFIRMED);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRepository.save(participation));
    }

    @Transactional
    public ParticipationRequestDto rejectParticipationRequest(Long userId, Long eventId, Long requestId) {
        checkUserInDb(userId);
        checkEventInDb(eventId);
        checkRequestInDb(requestId);
        checkInitiator(userId, eventId);
        ParticipationRequest participation = participationRepository.getReferenceById(requestId);
        participation.setStatus(Status.REJECTED);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRepository.save(participation));
    }

    private void checkInitiator(Long userId, Long eventId) {
        Event event = eventRepository.getReferenceById(eventId);
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
        Long confirmedRequests = participationRepository.countDistinctByEventAndStatus(event, Status.CONFIRMED);
        if (event.getParticipantLimit() != null && event.getParticipantLimit() != 0 && event
                .getParticipantLimit() <= confirmedRequests) {
            throw new ForbiddenException(String.format("превышено количество участников события - %d", confirmedRequests));
        }
    }

    private void checkEventInDb(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new NotFoundException(String.format("по id=%d данных нет", id));
        }
        log.info("PrivateParticipationRequestService: проверка существования события с id={} прошла успешно", id);
    }

    private void checkRequestInDb(Long requestId) {
        if (!participationRepository.existsById(requestId)) {
            throw new NotFoundException(String.format("запрса с id=%d в базе нет", requestId));
        }
        log.info("PrivateParticipationRequestService: проверка существования запроса с id={} прошла успешно", requestId);
    }

    private void checkUserInDb(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(String.format("по id=%d данных нет", id));
        }
        log.info("PrivateParticipationRequestService: проверка существования пользователя с id={} прошла успешно", id);
    }
}