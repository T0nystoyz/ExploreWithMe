package ru.practicum.main_server.service.private_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.client.StatisticClient;
import ru.practicum.main_server.exception.BadRequestException;
import ru.practicum.main_server.exception.ForbiddenException;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.EventMapper;
import ru.practicum.main_server.model.*;
import ru.practicum.main_server.model.dto.EventFullDto;
import ru.practicum.main_server.model.dto.EventShortDto;
import ru.practicum.main_server.model.dto.NewEventDto;
import ru.practicum.main_server.model.dto.UpdateEventRequest;
import ru.practicum.main_server.repository.CategoryRepository;
import ru.practicum.main_server.repository.EventRepository;
import ru.practicum.main_server.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PrivateEventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final StatisticClient statClient;
    private final CategoryRepository categoryRepository;
    private final PrivateLocationService locationService;

    public PrivateEventService(EventRepository eventRepository,
                               StatisticClient statClient, UserRepository userRepository,
                               CategoryRepository categoryRepository, PrivateLocationService locationService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.statClient = statClient;
        this.categoryRepository = categoryRepository;
        this.locationService = locationService;
    }

    public List<EventShortDto> readEvents(long userId, int from, int size) {
        log.info("PrivateEventService: чтение событий userId={}, from={}, size={}", userId, from, size);
        List<Event> e = statClient.getEventsWithViews(eventRepository.findAllByInitiatorId(userId,
                PageRequest.of(from / size, size)).toList());
        return e.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto updateEvent(Long userId, UpdateEventRequest updateEventRequest) {
        Event event = getEventFromRequest(userId, updateEventRequest);
        event = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(statClient.getViewsSingleEvent(updateEventRequest.getEventId()));
        log.info("PrivateEventService: событие обновлено userId={}, newEvent={}", userId, updateEventRequest);
        return eventFullDto;
    }

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Location location = newEventDto.getLocation();
        location = locationService.save(location);
        Event event = EventMapper.toNewEvent(newEventDto);
        validateEventDate(event);
        event.setInitiator(getUserFromDbOrThrow(userId));
        Category category = getCategoryFromDbOrThrow(newEventDto.getCategory());
        event.setCategory(category);
        event.setLocation(location);
        event.setConfirmedRequests(0);
        event.setCreatedOn(LocalDateTime.now());
        event = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        log.info("PrivateEventService: создано событие с именем {}", newEventDto.getTitle());
        return eventFullDto;
    }

    public EventFullDto readEvent(Long userId, Long eventId) {
        checkEventInitiator(userId, eventId);
        log.info("PrivateEventService: чтение пользователем с id={} события с id={}", userId, eventId);
        Event event = eventRepository.getReferenceById(eventId);
        event.setViews(statClient.getViewsSingleEvent(eventId));
        return EventMapper.toEventFullDto(event);
    }

    @Transactional
    public EventFullDto cancelEvent(Long userId, Long eventId) {
        Event event = getEventFromDbOrThrow(eventId);
        checkEventInitiator(userId, eventId);
        event.setState(State.CANCELED);
        event = eventRepository.save(event);
        event.setViews(statClient.getViewsSingleEvent(eventId));
        log.info("PrivateEventService: событие id={} отменено пользователем с id={}", eventId, userId);
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
    }*/

    /*private List<Event> getViewsMultipleEvents(List<Event> events) {
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
     * Возвращает событие из запроса
     *
     * @param userId             айди пользователя
     * @param updateEventRequest запрос пользователя
     * @return Event.class
     */
    private Event getEventFromRequest(Long userId, UpdateEventRequest updateEventRequest) {
        Event event = getEventFromDbOrThrow(updateEventRequest.getEventId());
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only creator can update event");
        }
        if (event.getState().equals(State.PUBLISHED)) {
            throw new BadRequestException("you can`t update published event");
        }
        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }
        if (updateEventRequest.getCategory() != null) {
            Category category = getCategoryFromDbOrThrow(updateEventRequest.getCategory());
            event.setCategory(category);
        }
        if (updateEventRequest.getEventDate() != null) {
            LocalDateTime date = LocalDateTime.parse(updateEventRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (date.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("дата события раньше чем через два часа от создания/обновления");
            }
            event.setEventDate(date);
        }
        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }
        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }
        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }
        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }
        return event;
    }

    /**
     * Проверяет является ли пользователь инициатором события
     *
     * @param userId  айди пользователя
     * @param eventId айди события
     */
    private void checkEventInitiator(Long userId, Long eventId) {
        Event event = getEventFromDbOrThrow(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("только инициатор может просомтреть полную информацию о событии");
        }
    }

    private Event getEventFromDbOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("PrivateEventService: события по id=%d нет в базе", id)));
    }

    private Category getCategoryFromDbOrThrow(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("PrivateEventService: категории по id=%d нет в базе", id)));
    }

    private User getUserFromDbOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("PrivateEventService: пользователя по id=%d нет в базе", id)));
    }

    private void validateEventDate(Event event) {
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("дата события должна быть позже чем через два часа от создания/обновления");
        }
    }
}
