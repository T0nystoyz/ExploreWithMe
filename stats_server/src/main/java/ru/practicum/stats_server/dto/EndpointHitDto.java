package ru.practicum.stats_server.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointHitDto {
    private Long id;
    private String app;
    private String uri;
    private String ip;
    private String timestamp;
}
