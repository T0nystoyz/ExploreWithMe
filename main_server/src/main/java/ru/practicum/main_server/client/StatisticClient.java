package ru.practicum.main_server.client;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.usertype.UserVersionType;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;

@Slf4j
@Service
public class StatisticClient extends BaseClient {

    @Autowired
    public StatisticClient(@Value("${statistics.url}") String serverUrl, RestTemplateBuilder builder) {
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

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end,
                                           List<String> uris, Boolean unique) throws UnsupportedEncodingException {
        Map<String, Object> parameters = Map.of(
                "start", URLEncoder.encode(start.toString(), StandardCharsets.UTF_8.toString()),
                "end", URLEncoder.encode(end.toString(), StandardCharsets.UTF_8.toString()),
                "uris", uris.get(0),
                "unique", unique
        );
        //String path = "localhost:9090/stats?start=" + ;
        log.info(":::::StatisticClient getStats-> start:{} end{}: uris:{}", start, end, uris);
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }
}

