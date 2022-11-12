package ru.practicum.main_server.model.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class ViewStats {
    @NotNull
    private String app;
    @NotNull
    private String uri;
    @NotNull
    private int hits;
}
