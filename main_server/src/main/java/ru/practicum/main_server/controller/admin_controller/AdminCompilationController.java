package ru.practicum.main_server.controller.admin_controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.model.dto.CompilationDto;
import ru.practicum.main_server.model.dto.NewCompilationDto;
import ru.practicum.main_server.service.admin_service.AdminCompilationService;


@RestController
@RequestMapping(path = "admin/compilations")
@Slf4j
public class AdminCompilationController {
    private final AdminCompilationService service;

    public AdminCompilationController(AdminCompilationService compilationService) {
        this.service = compilationService;
    }

    @PostMapping
    public CompilationDto createCompilation(@RequestBody NewCompilationDto newCompilationDto) {
        log.info(":::POST admin/compilations создание подборки {}", newCompilationDto);
        return service.createCompilation(newCompilationDto);
    }

    @PatchMapping("/{compId}/events/{eventId}")
    public void addEventToCompilation(@PathVariable Long compId,
                                      @PathVariable Long eventId) {
        log.info(":::PATCH admin/compilations/{}/events/{} добавление события в подборку", eventId, compId);
        service.addEventToCompilation(compId, eventId);
    }

    @PatchMapping("/{compId}/pin")
    public void pinCompilation(@PathVariable Long compId) {
        log.info(":::PATCH admin/compilations/{}/pin закрепить подборку", compId);
        service.pinCompilation(compId);
    }

    @DeleteMapping("/{compId}")
    public void deleteCompilation(@PathVariable Long compId) {
        log.info(":::DELETE admin/compilations/{} удаление подборки по id", compId);
        service.deleteCompilation(compId);
    }

    @DeleteMapping("/{compId}/events/{eventId}")
    public void deleteEventFromCompilation(@PathVariable Long compId,
                                           @PathVariable Long eventId) {
        log.info(":::DELETE admin/compilations/{}/events/{} удаление события из подборки", compId, eventId);
        service.deleteEventFromCompilation(compId, eventId);
    }

    @DeleteMapping("/{compId}/pin")
    public void unpinCompilation(@PathVariable Long compId) {
        log.info(":::DELETE admin/compilations/{}/pin снять подборку с закрепа", compId);
        service.unpinCompilation(compId);
    }
}