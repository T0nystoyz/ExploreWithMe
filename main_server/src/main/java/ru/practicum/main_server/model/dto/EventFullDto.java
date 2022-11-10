package ru.practicum.main_server.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.main_server.model.Location;

@Builder
@Getter
@Setter
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private String createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private Location location;
    private boolean paid;
    private int participantLimit;
    private String publishedOn;
    private boolean requestModeration;
    private String state;
    private String title;
    private Integer views;
}
