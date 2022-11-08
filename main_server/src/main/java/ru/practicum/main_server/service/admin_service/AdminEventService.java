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
import ru.practicum.main_server.model.State;
import ru.practicum.main_server.model.dto.AdminUpdateEventRequest;
import ru.practicum.main_server.model.dto.EventFullDto;
import ru.practicum.main_server.repository.CategoryRepository;
import ru.practicum.main_server.repository.EventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AdminEventService {
    private final EventRepository eventRepository;
    private final StatisticClient statClient;
    private final CategoryRepository categoryRepository;

    @Autowired
    public AdminEventService(EventRepository eventRepository,
                             StatisticClient statClient, CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.statClient = statClient;
        this.categoryRepository = categoryRepository;
    }

    public List<EventFullDto> readEvents(List<Long> users, List<State> states, List<Long> categories,
                                         String rangeStart, String rangeEnd, int from, int size) {
        LocalDateTime start = getStartTime(rangeStart);
        LocalDateTime end = getEndTime(rangeEnd);
        log.info("AdminEventService: чтение всех событий, from: {}, size: {}", from, size);
        List<Event> e = statClient.getEventsWithViews(eventRepository.searchEventsByAdmin(users, states, categories, start,
                end, PageRequest.of(from / size, size)).toList());
        return e.stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }

    public EventFullDto updateEvent(Long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {
        Event event = getEventFromAdminRequest(eventId, adminUpdateEventRequest);
        event = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(statClient.getViewsSingleEvent(eventId));
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
        event = eventRepository.save(event);
        EventFullDto dto = EventMapper.toEventFullDto(event);
        dto.setViews(statClient.getViewsSingleEvent(eventId));
        return dto;
    }

    public EventFullDto rejectEvent(Long eventId) {
        Event event = getEventFromDbOrThrow(eventId);
        event.setState(State.CANCELED);
        event = eventRepository.save(event);
        log.info("AdminEventService: отклонение события с id={}", eventId);
        return EventMapper.toEventFullDto(event);
    }


    /*private Integer getViewsSingleEvent(long eventId) {
        List<ViewStats> stats;
        try {
            stats = statClient.getStats(
                    eventRepository.getReferenceById(eventId).getCreatedOn(),
                    LocalDateTime.now(),
                    List.of("/events/" + eventId),
                    false);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException("неудачная кодировка");
        }
        if (!stats.isEmpty()) {
            log.info("::::stats={}", stats);
            return stats.get(0).getHits();
        }
        return 0;
    }

    private List<Event> getViewsMultipleEvents(List<Event> events) {
        List<ViewStats> stats;
        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());
        try {
            stats = statClient.getStats(
                    (Collections.min(events, Comparator.comparing(Event::getCreatedOn)).getCreatedOn()),
                    LocalDateTime.now(),
                    uris,
                    false);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException("неудачная кодировка");
        }
        if (!stats.isEmpty()) {
            for (int i = 0; i < stats.size(); i++) {
                events.get(i).setViews(stats.get(i).getHits());
            }
        }
        return events;
    }*/

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
            end = LocalDateTime.now().plusYears(5);
        } else {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return end;
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
