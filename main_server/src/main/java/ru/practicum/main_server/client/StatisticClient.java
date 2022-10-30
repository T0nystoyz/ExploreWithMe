package ru.practicum.main_server.client;

import ru.practicum.main_server.model.dto.EndpointHitDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;

@Service
public class StatisticClient extends BaseClient {

    @Autowired
    public StatisticClient(@Value("${STAT_SERVER_URL}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public void createHit(EndpointHitDto endpointHit) {
        post("/hit", endpointHit);
    }

    public ResponseEntity<Object> getStats(String start, String end,
                                           List<String> uris, Boolean unique) throws UnsupportedEncodingException {
        Map<String, Object> parameters = Map.of(
                "start", URLEncoder.encode(start, StandardCharsets.UTF_8.toString()),
                "end", URLEncoder.encode(end, StandardCharsets.UTF_8.toString()),
                "uris", uris.get(0),
                "unique", unique
        );
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }
}

