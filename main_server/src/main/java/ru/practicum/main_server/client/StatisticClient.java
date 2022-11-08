package ru.practicum.main_server.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.model.dto.EndpointHitDto;
import ru.practicum.main_server.model.dto.ViewStats;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StatisticClient {
    protected final RestTemplate rest;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatisticClient(@Value("${STATS_SERVER_URL}") String serverUrl, RestTemplateBuilder builder) {
        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void createHit(EndpointHitDto endpointHit) {
        rest.postForEntity("/hit", endpointHit, Object.class);
    }

    /*public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end,
                                           List<String> uris, Boolean unique) throws UnsupportedEncodingException {
        Map<String, Object> parameters = Map.of(
                "start", URLEncoder.encode(start.format(formatter), StandardCharsets.UTF_8.toString()),
                "end", URLEncoder.encode(end.format(formatter), StandardCharsets.UTF_8.toString()),
                "uris", uris,
                "unique", unique
        );
        log.info(":::::StatisticClient getStats-> parameters:{}", parameters);
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }*/

    public ViewStats[] getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique)
            throws UnsupportedEncodingException {
        ViewStats[] stats = rest.getForObject(
                "/stats?start=" + URLEncoder.encode(start.format(formatter), StandardCharsets.UTF_8.toString()) +
                        "&end=" + URLEncoder.encode(end.format(formatter), StandardCharsets.UTF_8.toString()) +
                        "&uris=" + uris, ViewStats[].class);
        if (stats == null) {
            throw new NotFoundException(String.format("По данным URL %s статистики не найдено", uris));
        }
        return stats;
    }
}

