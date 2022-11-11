package ru.practicum.main_server.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.main_server.exception.InternalServerErrorException;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.dto.EndpointHitDto;
import ru.practicum.main_server.model.dto.ViewStats;
import ru.practicum.main_server.repository.EventRepository;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticClient {
    protected final RestTemplate rest;
    private final EventRepository eventRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatisticClient(@Value("${STATS_SERVER_URL}") String serverUrl, RestTemplateBuilder builder,
                           EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void createHit(EndpointHitDto endpointHit) {
        rest.postForEntity("/hit", endpointHit, Object.class);
    }

    private List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique)
            throws UnsupportedEncodingException {
        ResponseEntity<List<ViewStats>> responseEntity =
                rest.exchange(
                        "/stats?start=" + URLEncoder.encode(start.format(formatter),
                                StandardCharsets.UTF_8.toString()) +
                                "&end=" + URLEncoder.encode(end.format(formatter),
                                StandardCharsets.UTF_8.toString()) +
                                "&uris=" + uris +
                                "&unique=" + unique,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        }
                );
        log.info(":::::StatisticClient getStats-> response:{}", responseEntity);
        return responseEntity.getBody();
    }

    /**
     * Обращается к серверу статистики для получения кол-ва просмотров события
     *
     * @param eventId айди события
     * @return int - количество просмотров
     */
    public Integer getViewsSingleEvent(long eventId) {
        List<ViewStats> stats;
        try {
            stats = getStats(
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

    /**
     * Обращается к серверу статистики для преобразования списка событий с полями views.
     *
     * @param events List
     * @return List<Event> - события с просмотрами
     */
    public List<Event> getEventsWithViews(List<Event> events) {
        List<ViewStats> stats;
        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());
        try {
            stats = getStats(
                    LocalDateTime.now().minusDays(365),
                    LocalDateTime.now(),
                    uris,
                    false);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException("неудачная кодировка");
        }

        if (!stats.isEmpty()) {
            log.info("stats не пустой");
            Map<String, Event> eventsMap = events.stream().collect(Collectors.toMap(e -> "/events/" + e.getId(),
                    Function.identity()));
            Map<String, Integer> statsMap = stats.stream()
                    .collect(Collectors.toMap(ViewStats::getUri, ViewStats::getHits));
            for (Map.Entry<String, Integer> entry : statsMap.entrySet()) {
                Event e = eventsMap.get(entry.getKey());
                e.setViews(entry.getValue());
            }
            return new ArrayList<>(eventsMap.values());
        }
        log.info("stats пустой");
        return events;
    }

}