package ru.practicum.main_server.model.dto;

import lombok.*;
import ru.practicum.main_server.model.Location;

import javax.validation.constraints.NotNull;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    private String annotation;
    private long category;
    private String description;
    @NotNull
    private String eventDate;
    private Location location;
    private boolean paid;
    private int participantLimit;
    private boolean requestModeration;
    private String state;
    private String title;
}
