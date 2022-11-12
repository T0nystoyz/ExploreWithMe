package ru.practicum.main_server.controller.private_controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.model.dto.ParticipationRequestDto;
import ru.practicum.main_server.service.private_service.PrivateParticipationRequestService;

import java.util.List;


@RestController
@RequestMapping(path = "/users/{userId}/requests")
@Slf4j
public class PrivateParticipationRequestController {
    private final PrivateParticipationRequestService service;

    public PrivateParticipationRequestController(PrivateParticipationRequestService service) {
        this.service = service;
    }

    @GetMapping
    public List<ParticipationRequestDto> readParticipationRequests(@PathVariable Long userId) {
        log.info(":::GET /users/{}/requests чтение запросов на участие пользователя по id", userId);
        return service.readParticipationRequests(userId);
    }

    @PostMapping
    public ParticipationRequestDto createParticipationRequest(@PathVariable Long userId,
                                                              @RequestParam(name = "eventId") Long eventId) {
        log.info(":::POST /users/{}/requests создание запроса пользователем, eventId={}", userId, eventId);
        return service.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.info(":::PATCH /users/{}/requests/{}/cancel отмена запроса", userId, requestId);
        return service.cancelRequest(userId, requestId);
    }
}