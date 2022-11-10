package ru.practicum.main_server.model.dto;

import lombok.*;
import ru.practicum.main_server.model.Location;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NonNull
    private String annotation;
    private long category;
    @NotNull
    private String description;
    @NotNull
    private String eventDate;
    private Location location;
    private boolean paid;
    @Positive
    private Long participantLimit;
    private boolean requestModeration;
    private String state;
    @NotNull
    private String title;
}
