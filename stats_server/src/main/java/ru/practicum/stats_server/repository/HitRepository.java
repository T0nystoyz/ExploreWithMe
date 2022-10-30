package ru.practicum.stats_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats_server.model.EndpointHit;
import ru.practicum.stats_server.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT e.app AS app, e.uri AS uri, count(e.uri) AS hits " +
            "FROM EndpointHit e " +
            "WHERE (e.timestamp BETWEEN :start AND :end) " +
            "AND e.uri IN :uris " +
            "GROUP BY (e.app), (e.uri)")
    List<ViewStats> getViewStatsListByParams(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT e.app AS app, e.uri AS uri, count(e.uri) AS hits " +
            "FROM EndpointHit e " +
            "WHERE (e.timestamp BETWEEN :start AND :end) " +
            "AND e.uri IN :uris " +
            "GROUP BY (e.app), (e.uri), (e.ip)")
    List<ViewStats> getViewStatsListByParamsUnique(LocalDateTime start, LocalDateTime end, List<String> uris);
}
