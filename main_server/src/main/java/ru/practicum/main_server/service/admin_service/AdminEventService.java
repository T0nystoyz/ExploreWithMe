package ru.practicum.main_server.service.admin_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.client.StatisticClient;
import ru.practicum.main_server.exception.ForbiddenException;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.EventMapper;
import ru.practicum.main_server.model.Category;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.ParticipationRequest;
import ru.practicum.main_server.model.State;
import ru.practicum.main_server.model.dto.AdminUpdateEventRequest;
import ru.practicum.main_server.model.dto.EventFullDto;
import ru.practicum.main_server.repository.CategoryRepository;
import ru.practicum.main_server.repository.EventRepository;
import ru.practicum.main_server.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.main_server.model.Status.CONFIRMED;

@Service
@Slf4j
@Transactional
public class AdminEventService {
    private final EventRepository eventRepository;
    private final StatisticClient statClient;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository participationRequestRepository;

    @Autowired
    public AdminEventService(EventRepository eventRepository,
                             StatisticClient statClient, CategoryRepository categoryRepository,
                             ParticipationRequestRepository participationRequestRepository) {
        this.eventRepository = eventRepository;
        this.statClient = statClient;
        this.categoryRepository = categoryRepository;
        this.participationRequestRepository = participationRequestRepository;
    }

    public List<EventFullDto> readEvents(List<Long> users, List<State> states, List<Long> categories,
                                         String rangeStart, String rangeEnd, int from, int size) {
        LocalDateTime start = getStartTime(rangeStart);
        LocalDateTime end = getEndTime(rangeEnd);
        log.info("AdminEventService: чтение всех событий, from: {}, size: {}", from, size);
        List<Event> e = statClient.getEventsWithViews(eventRepository.searchEventsByAdmin(users, states, categories,
                start, end, PageRequest.of(from / size, size)).toList());
        log.info("////eventsWithViews{}", e);
        List<Event> eventsWithRequests = getEventsWithConfirmedRequests(e);
        log.info("////eventsWithRequests{}", eventsWithRequests);
        return eventsWithRequests.stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }

    public EventFullDto updateEvent(Long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {
        Event event = getEventFromAdminRequest(eventId, adminUpdateEventRequest);
        event = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(statClient.getViewsSingleEvent(eventId));
        eventFullDto.setConfirmedRequests(participationRequestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
        log.info("AdminEventService: обновление события с id={}, запрос: {}", eventId, adminUpdateEventRequest);
        return eventFullDto;
    }

    public EventFullDto publishEvent(Long eventId) {
        Event event = getEventFromDbOrThrow(eventId);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ForbiddenException("дата события раньше чем через два часа от создания/обновления");
        }
        if (!event.getState().equals(State.PENDING)) {
            throw new ForbiddenException("событие не ожидает побликации");
        }
        log.info("AdminEventService: публикация события с id={}", eventId);
        event.setState(State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event);
    }

    public EventFullDto rejectEvent(Long eventId) {
        Event event = getEventFromDbOrThrow(eventId);
        event.setState(State.CANCELED);
        event = eventRepository.save(event);
        log.info("AdminEventService: отклонение события с id={}", eventId);
        return EventMapper.toEventFullDto(event);
    }


    /**
     * Возвращает событие из запроса админа
     *
     * @param eventId                 айди события
     * @param adminUpdateEventRequest запрос админа
     * @return Event.class
     */
    private Event getEventFromAdminRequest(Long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {
        Event event = getEventFromDbOrThrow(eventId);
        if (adminUpdateEventRequest.getAnnotation() != null) {
            event.setAnnotation(adminUpdateEventRequest.getAnnotation());
        }
        if (adminUpdateEventRequest.getCategory() != null) {
            Category category = getCategoryFromDbOrThrow(adminUpdateEventRequest.getCategory());
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
     * Возвращает время старта из строки
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
     * Возвращает время окончания из строки
     *
     * @param rangeEnd принимает строку даты
     * @return LocalDateTime.class
     */
    private LocalDateTime getEndTime(String rangeEnd) {
        LocalDateTime end;
        if (rangeEnd == null) {
            end = LocalDateTime.now().plusYears(5);
        } else {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return end;
    }

    /**
     * @param events список событий
     * @return List событий с полями confirmedRequests
     */
    private List<Event> getEventsWithConfirmedRequests(List<Event> events) {
        Map<Long, Event> eventsWithRequests = events.stream().collect(Collectors.toMap(Event::getId, Function.identity()));
        Map<Event, Long> countedRequests = participationRequestRepository
                .findByStatusAndEvent(CONFIRMED, events).stream()
                .collect(Collectors.groupingBy(ParticipationRequest::getEvent, Collectors.counting()));
        if (countedRequests.isEmpty()) {
            return events;
        }
        for (Map.Entry<Event, Long> entry : countedRequests.entrySet()) {
            Event e = entry.getKey();
            e.setConfirmedRequests(entry.getValue());
            eventsWithRequests.put(e.getId(), e);
        }
        log.info("////eventsWithRequests{}",eventsWithRequests);
        return new ArrayList<>(eventsWithRequests.values());
    }

    private Event getEventFromDbOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("AdminCompilationService: события по id=%d нет в базе", id)));
    }

    private Category getCategoryFromDbOrThrow(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("AdminEventService: категории по id=%d нет в базе", id)));
    }
}
