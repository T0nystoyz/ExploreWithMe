package ru.practicum.stats_server.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats_server.dto.EndpointHitDto;
import ru.practicum.stats_server.dto.ViewStats;
import ru.practicum.stats_server.service.StatisticService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
public class StatisticController {
    private final StatisticService statisticService;

    @Autowired
    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @PostMapping("/hit")
    public EndpointHitDto createHit(@RequestBody @Valid EndpointHitDto endpointHit) {
        log.info(":::POST /hit StatisticController: создание просмотра: {}", endpointHit);
        return statisticService.addHit(endpointHit);
    }

    @SneakyThrows
    @GetMapping("/stats")
    public List<ViewStats> getViewStats(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam List<String> uris,
                                        @RequestParam(defaultValue = "false") Boolean unique) {
        log.info(":::GET /stats StatisticController: получение статистики по адресам: {}, start={}, end={}", uris, start,end);
        return statisticService.getViewStats(start, end, uris, unique);
    }
}
