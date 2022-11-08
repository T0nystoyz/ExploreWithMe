package ru.practicum.main_server.service.public_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.client.StatisticClient;
import ru.practicum.main_server.exception.BadRequestException;
import ru.practicum.main_server.exception.InternalServerErrorException;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.EventMapper;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.State;
import ru.practicum.main_server.model.dto.EndpointHitDto;
import ru.practicum.main_server.model.dto.EventFullDto;
import ru.practicum.main_server.model.dto.EventShortDto;
import ru.practicum.main_server.model.dto.ViewStats;
import ru.practicum.main_server.repository.EventRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class PublicEventService {
    private final EventRepository eventRepository;
    private final StatisticClient statClient;


    @Autowired
    public PublicEventService(EventRepository eventRepository,
                              StatisticClient statClient) {
        this.eventRepository = eventRepository;
        this.statClient = statClient;
    }

    public List<EventShortDto> readEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                          String rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        LocalDateTime start = getStartTime(rangeStart);
        LocalDateTime end = getEndTime(rangeEnd);

        List<Event> events = getViewsMultipleEvents(eventRepository.searchEvents(text, categories, paid, start, end,
                        PageRequest.of(from / size, size))
                .stream()
                .collect(Collectors.toList()));
        if (sort != null && sort.equals("EVENT_DATE")) {
            events = events.stream()
                    .sorted(Comparator.comparing(Event::getEventDate))
                    .collect(Collectors.toList());
        }

        List<EventShortDto> listShortDto = events.stream()
                .filter(event -> event.getState().equals(State.PUBLISHED))
                .map(EventMapper::toEventShortDto)
                //.peek(e -> e.setViews(getViews(e.getId())))
                .collect(Collectors.toList());
        if (sort != null && sort.equals("VIEWS")) {
            listShortDto = listShortDto.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }
        if (onlyAvailable != null && onlyAvailable) {
            listShortDto = listShortDto.stream()
                    .filter(eventShortDto -> eventShortDto.getConfirmedRequests()
                            < eventShortDto.getParticipationLimit())
                    .collect(Collectors.toList());
        }
        return listShortDto;
    }

    public EventFullDto readEvent(long id) {
        EventFullDto dto = EventMapper.toEventFullDto(getEventFromDbOrThrow(id));
        if (!(dto.getState().equals(State.PUBLISHED.toString()))) {
            throw new BadRequestException("можно посмотреть только опубликованные события");
        }
        dto.setViews(getViewsSingleEvent(id));
        return dto;
    }

    /**
     * Отправляет данные в сервис статистики
     *
     * @param request - запрос http
     */
    public void sentHitStat(HttpServletRequest request) {
        log.info("request URL {}", request.getRequestURI());
        EndpointHitDto endpointHit = EndpointHitDto.builder()
                .app("main_server")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        statClient.createHit(endpointHit);
    }

    private LocalDateTime getEndTime(String rangeEnd) {
        LocalDateTime end;
        if (rangeEnd == null) {
            end = LocalDateTime.now().plusYears(5);
        } else {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return end;
    }

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
     * Обращается к серверу статистики для получения кол-ва просмотров события
     *
     * @param eventId айди события
     * @return int - количество просмотров
     */
    private Integer getViewsSingleEvent(long eventId) {
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
    }

    private Event getEventFromDbOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("AdminCompilationService: события по id=%d нет в базе", id)));
    }
}
