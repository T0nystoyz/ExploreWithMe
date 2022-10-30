package ru.practicum.main_server.service.admin_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.mapper.CompilationMapper;
import ru.practicum.main_server.model.Compilation;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.dto.CompilationDto;
import ru.practicum.main_server.model.dto.EventShortDto;
import ru.practicum.main_server.model.dto.NewCompilationDto;
import ru.practicum.main_server.repository.CompilationRepository;
import ru.practicum.main_server.repository.EventRepository;
import ru.practicum.main_server.service.public_service.PublicEventService;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AdminCompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final PublicEventService eventService;

    @Autowired
    public AdminCompilationService(CompilationRepository compilationRepository, EventRepository eventRepository,
                                   PublicEventService eventService) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
    }

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("AdminCompilationService: создание подборки {}", newCompilationDto);
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        newCompilationDto.getEvents().forEach(this::checkEventInDb);
        Set<Event> events = newCompilationDto.getEvents().stream()
                .map(eventRepository::getReferenceById)
                .collect(Collectors.toSet());
        compilation.setEvents(events);
        Compilation newCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(newCompilation);
        return setViewsAndConfirmedRequests(compilationDto);
    }

    public void deleteCompilation(Long id) {
        checkCompilationInDb(id);
        compilationRepository.deleteCompilationById(id);
        log.info("AdminCompilationService: удаление подборки с id={}", id);
    }

    public void deleteEventFromCompilation(Long compId, Long eventId) {
        log.info("AdminCompilationService: удаление события id={} из подборки id={}", eventId, compId);
        checkCompilationInDb(compId);
        checkEventInDb(eventId);
        Compilation compilation = compilationRepository.getReferenceById(compId);
        Set<Event> events = compilation.getEvents();
        events.remove(eventRepository.getReferenceById(eventId));
        compilation.setEvents(events);
        compilationRepository.save(compilation);
    }

    public void addEventToCompilation(Long compId, Long eventId) {
        checkEventInDb(eventId);
        checkCompilationInDb(compId);
        Compilation compilation = compilationRepository.getReferenceById(compId);
        Set<Event> events = compilation.getEvents();
        events.add(eventRepository.getReferenceById(eventId));
        compilationRepository.save(compilation);
    }

    public void unpinCompilation(Long compId) {
        checkCompilationInDb(compId);
        Compilation compilation = compilationRepository.getReferenceById(compId);
        compilation.setPinned(false);
        compilationRepository.save(compilation);
    }

    public void pinCompilation(Long compId) {
        checkCompilationInDb(compId);
        Compilation compilation = compilationRepository.getReferenceById(compId);
        compilation.setPinned(true);
        compilationRepository.save(compilation);
    }

    /**
     * Возвращает компиляцию с заполненными полями событий: кол-во просмотров и принявших учатсие
     *
     * @param compilationDto входные данные компиляции
     * @return CompilationDto.class
     */
    private CompilationDto setViewsAndConfirmedRequests(CompilationDto compilationDto) {
        List<EventShortDto> listShortDto = new ArrayList<>();
        for (EventShortDto eventShortDto : compilationDto.getEvents()) {
            EventShortDto shortDto = eventService.setConfirmedRequestsAndViewsEventShortDto(eventShortDto);
            listShortDto.add(shortDto);
        }
        compilationDto.setEvents(listShortDto);
        return compilationDto;
    }

    private void checkCompilationInDb(Long id) {
        if (!compilationRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    String.format("AdminCompilationService: подборки по id=%d нет в базе", id));
        }
        log.info("AdminCompilationService: проверка существования подборки с id={} прошла успешно", id);
    }

    private void checkEventInDb(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    String.format("AdminCompilationService: события по id=%d нет в базе", id));
        }
        log.info("AdminCompilationService: проверка существования события с id={} прошла успешно", id);
    }

}
