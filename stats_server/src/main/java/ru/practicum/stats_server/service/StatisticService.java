package ru.practicum.stats_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats_server.model.EndpointHit;
import ru.practicum.stats_server.dto.ViewStats;
import ru.practicum.stats_server.repository.HitRepository;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StatisticService {
    private final HitRepository repository;

    public List<ViewStats> getViewStats(String start, String end, List<String> uris, Boolean unique)
            throws UnsupportedEncodingException {

        LocalDateTime startTime;
        LocalDateTime endTime;

        String decodeStart = URLDecoder.decode(start, StandardCharsets.UTF_8.toString());
        String decodeEnd = URLDecoder.decode(end, StandardCharsets.UTF_8.toString());
        startTime = LocalDateTime.parse(decodeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        endTime = LocalDateTime.parse(decodeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info(":::декодирование прошло успешно {} -> {}", start, decodeStart);

        if (unique) {
            return repository.getViewStatsListByParamsUnique(startTime, endTime, uris);
        }
        return repository.getViewStatsListByParams(startTime, endTime, uris);
    }

    public EndpointHit addHit(EndpointHit hit) {
        return repository.save(hit);
    }
}
