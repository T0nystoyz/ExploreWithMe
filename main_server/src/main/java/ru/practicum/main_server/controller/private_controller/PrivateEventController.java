package ru.practicum.main_server.controller.private_controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.model.dto.*;
import ru.practicum.main_server.service.private_service.PrivateCommentService;
import ru.practicum.main_server.service.private_service.PrivateEventService;
import ru.practicum.main_server.service.private_service.PrivateParticipationRequestService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@Slf4j
public class PrivateEventController {
    private final PrivateEventService privateEventService;
    private final PrivateParticipationRequestService participationRequestService;
    private final PrivateCommentService commentService;

    public PrivateEventController(PrivateEventService privateEventService,
                                  PrivateParticipationRequestService participationService, PrivateCommentService commentService) {
        this.privateEventService = privateEventService;
        this.participationRequestService = participationService;
        this.commentService = commentService;
    }

    @PostMapping
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @RequestBody @Valid NewEventDto newEventDto) {
        log.info(":::POST /users/{}/events создание события пользователя ", userId);
        return privateEventService.createEvent(userId, newEventDto);
    }

    @GetMapping()
    public List<EventShortDto> readEvents(@PathVariable long userId,
                                   @RequestParam(defaultValue = "0") int from,
                                   @RequestParam(defaultValue = "10") int size) {
        log.info(":::GET /users/{}/events чтение всех событий пользователя", userId);
        return privateEventService.readEvents(userId, from, size);
    }

    @PatchMapping
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @RequestBody UpdateEventRequest updateEventRequest) {
        log.info(":::PATCH /users/{}/events редактирование события пользователя ", userId);
        return privateEventService.updateEvent(userId, updateEventRequest);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventCurrentUser(@PathVariable Long userId,
                                            @PathVariable Long eventId) {
        log.info(":::GET /users/{}/events/{} чтение своего события по айди", userId, eventId);
        return privateEventService.readEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto cancelEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId) {
        log.info(":::PATCH /users/{}/events/{} отмена пользователем события по айди", userId, eventId);
        return privateEventService.cancelEvent(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipationByOwner(@PathVariable Long userId,
                                                                      @PathVariable Long eventId) {
        log.info(":::PATCH /users/{}/events/{}/requests чтение пользователем запросов на " +
                "участие в событии", userId, eventId);
        return participationRequestService.readEventParticipation(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/confirm")
    public ParticipationRequestDto approvalParticipationEventRequest(@PathVariable Long userId,
                                                                     @PathVariable Long eventId,
                                                                     @PathVariable Long reqId) {
        log.info(":::PATCH /users/{}/events/{}/requests/{}/confirm принять запрос на участие",
                userId, eventId, reqId);
        return participationRequestService.approveParticipationRequest(userId, eventId, reqId);
    }

    @PatchMapping("/{eventId}/requests/{reqId}/reject")
    public ParticipationRequestDto rejectParticipationEventRequest(@PathVariable Long userId,
                                                                   @PathVariable Long eventId,
                                                                   @PathVariable Long reqId) {
        log.info(":::PATCH /users/{}/events/{}/requests/{}/reject отклонить запрос на участие",
                userId, eventId, reqId);
        return participationRequestService.rejectParticipationRequest(userId, eventId, reqId);
    }

    @GetMapping("/{eventId}/comments/")
    public List<CommentDto> readEventComments(@PathVariable Long userId, @PathVariable Long eventId){
        return commentService.readEventComments(eventId);
    }
}
