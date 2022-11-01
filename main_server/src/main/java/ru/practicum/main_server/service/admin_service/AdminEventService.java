package ru.practicum.main_server.service.admin_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.main_server.client.StatisticClient;
import ru.practicum.main_server.exception.ForbiddenException;
import ru.practicum.main_server.exception.InternalServerErrorException;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.EventMapper;
import ru.practicum.main_server.model.*;
import ru.practicum.main_server.model.dto.*;
import ru.practicum.main_server.repository.CategoryRepository;
import ru.practicum.main_server.repository.EventRepository;
import ru.practicum.main_server.repository.ParticipationRequestRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AdminEventService {
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRepository;
    private final StatisticClient statClient;
    private final CategoryRepository categoryRepository;


    @Autowired
    public AdminEventService(EventRepository eventRepository, ParticipationRequestRepository participationRepository,
                             StatisticClient statClient, CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.participationRepository = participationRepository;
        this.statClient = statClient;
        this.categoryRepository = categoryRepository;
    }

    public List<EventFullDto> readEvents(List<Long> users, List<State> states, List<Long> categories,
                                         String rangeStart, String rangeEnd, int from, int size) {
        LocalDateTime start = getStartTime(rangeStart);
        LocalDateTime end = getEndTime(rangeEnd);
        log.info("AdminEventService: чтение всех событий, from: {}, size: {}", from, size);
        return eventRepository.searchEventsByAdmin(users, states, categories, start, end,
                        PageRequest.of(from / size, size))
                .stream()
                .map(EventMapper::toEventFullDto)
                .map(this::setConfirmedRequestsAndViews)
                .collect(Collectors.toList());
    }

    public EventFullDto updateEvent(Long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {
        checkEventInDb(eventId);
        Event event = getEventFromAdminRequest(eventId, adminUpdateEventRequest);
        event = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        log.info("AdminEventService: обновление события с id={}, запрос: {}", eventId, adminUpdateEventRequest);
        return setConfirmedRequestsAndViews(eventFullDto);
    }

    public EventFullDto publishEvent(Long eventId) {
        checkEventInDb(eventId);
        Event event = eventRepository.getReferenceById(eventId);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ForbiddenException("дата события раньше чем через два часа от создания/обновления");
        }
        if (!event.getState().equals(State.PENDING)) {
            throw new ForbiddenException("событие не ожидает побликации");
        }
        log.info("AdminEventService: публикация события с id={}", eventId);
        event.setState(State.PUBLISHED);
        event = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        return eventFullDto;
    }

    public EventFullDto rejectEvent(Long eventId) {
        checkEventInDb(eventId);
        Event event = eventRepository.getReferenceById(eventId);
        event.setState(State.CANCELED);
        event = eventRepository.save(event);
        //EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        log.info("AdminEventService: отклонение события с id={}", eventId);
        return EventMapper.toEventFullDto(event);
    }

    /**
     * Возвращает событие с полями просмотров и подтвержденных учатсников
     *
     * @param eventFullDto - полный DTO события
     * @return EventFullDto.class
     */
    private EventFullDto setConfirmedRequestsAndViews(EventFullDto eventFullDto) {
        Long confirmedRequests = participationRepository
                .countByEventIdAndStatus(eventFullDto.getId(), Status.CONFIRMED);
        eventFullDto.setConfirmedRequests(confirmedRequests);
        eventFullDto.setViews(getViews(eventFullDto.getId()));
        return eventFullDto;
    }

    /**
     * Возвращает кол-во просмотров события
     *
     * @param eventId айди события
     * @return int - количество просмотров
     */
    private int getViews(long eventId) {
        ResponseEntity<Object> responseEntity;
        try {
            responseEntity = statClient.getStats(
                    eventRepository.getReferenceById(eventId).getEventDate(),
                    LocalDateTime.now(),
                    List.of("/events/" + eventId), false);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException("ошибка кодирования URL");
        }
        if (Objects.equals(responseEntity.getBody(), "")) {
            return (Integer) ((LinkedHashMap<?, ?>) responseEntity.getBody()).get("hits");
        }
        return 0;
    }

    /**
     * Возвращает событие из запроса админа
     *
     * @param eventId                 айди события
     * @param adminUpdateEventRequest запрос админа
     * @return Event.class
     */
    private Event getEventFromAdminRequest(Long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {
        checkEventInDb(eventId);
        checkCategoryInDb(adminUpdateEventRequest.getCategory());
        Event event = eventRepository.getReferenceById(eventId);
        if (adminUpdateEventRequest.getAnnotation() != null) {
            event.setAnnotation(adminUpdateEventRequest.getAnnotation());
        }
        if (adminUpdateEventRequest.getCategory() != null) {
            Category category = categoryRepository.getReferenceById(adminUpdateEventRequest.getCategory());
            event.setCategory(category);
        }
        if (adminUpdateEventRequest.getDescription() != null) {
            event.setDescription(adminUpdateEventRequest.getDescription());
        }
        if (adminUpdateEventRequest.getEventDate() != null) {
            LocalDateTime date = LocalDateTime.parse(adminUpdateEventRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (date.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ForbiddenException("дата события раньше чем через два часа от создания/обновления");
            }
            event.setEventDate(date);
        }
        if (adminUpdateEventRequest.getLocation() != null) {
            event.setLocation(adminUpdateEventRequest.getLocation());
        }
        if (adminUpdateEventRequest.getRequestModeration() != null) {
            event.setRequestModeration(adminUpdateEventRequest.getRequestModeration());
        }
        if (adminUpdateEventRequest.getPaid() != null) {
            event.setPaid(adminUpdateEventRequest.getPaid());
        }
        if (adminUpdateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(adminUpdateEventRequest.getParticipantLimit());
        }
        if (adminUpdateEventRequest.getTitle() != null) {
            event.setTitle(adminUpdateEventRequest.getTitle());
        }
        return event;
    }

    /**
     * Возвращает время старта либо null
     *
     * @param rangeStart принимает строку даты
     * @return LocalDateTime.class
     */
    private LocalDateTime getStartTime(String rangeStart) {
        LocalDateTime start;
        if (rangeStart == null) {
            start = LocalDateTime.now();
        } else {
            start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return start;
    }

    /**
     * Возвращает время окончания либо null
     *
     * @param rangeEnd принимает строку даты
     * @return LocalDateTime.class
     */
    private LocalDateTime getEndTime(String rangeEnd) {
        LocalDateTime end;
        if (rangeEnd == null) {
            end = LocalDateTime.MAX;
        } else {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return end;
    }

    private void checkEventInDb(long id) {
        if (!eventRepository.existsById(id)) {
            throw new NotFoundException(String.format("по даному id=%d данных в базе нет", id));
        }
    }

    private void checkCategoryInDb(long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException(String.format("по даному id=%d данных в базе нет", id));
        }
    }
}
