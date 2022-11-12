package ru.practicum.main_server.controller.public_controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.model.dto.CompilationDto;
import ru.practicum.main_server.service.public_service.PublicCompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@Slf4j
public class PublicCompilationController {
    private final PublicCompilationService service;

    public PublicCompilationController(PublicCompilationService service) {
        this.service = service;
    }

    @GetMapping()
    List<CompilationDto> readCompilations(@RequestParam(required = false) Boolean pinned,
                                          @RequestParam(defaultValue = "0") int from,
                                          @RequestParam(defaultValue = "10") int size) {
        log.info(":::GET /compilations чтение подборок pinned={}, from={}, size={}", pinned, from, size);
        return service.readCompilations(pinned, from, size);
    }

    @GetMapping("/{id}")
    CompilationDto readCompilation(@PathVariable long id) {
        log.info(":::GET /compilations/{} чтение подборки по id", id);
        return service.readCompilation(id);
    }
}
