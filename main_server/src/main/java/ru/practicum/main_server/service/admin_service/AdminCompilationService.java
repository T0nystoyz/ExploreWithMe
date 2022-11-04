package ru.practicum.main_server.service.admin_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.CompilationMapper;
import ru.practicum.main_server.model.Compilation;
import ru.practicum.main_server.model.Event;
import ru.practicum.main_server.model.dto.CompilationDto;
import ru.practicum.main_server.model.dto.NewCompilationDto;
import ru.practicum.main_server.repository.CompilationRepository;
import ru.practicum.main_server.repository.EventRepository;

import java.util.List;

@Slf4j
@Service
@Transactional
public class AdminCompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Autowired
    public AdminCompilationService(CompilationRepository compilationRepository, EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("AdminCompilationService: создание подборки {}", newCompilationDto);
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        /*Set<Event> events = newCompilationDto.getEvents().stream()
                .map(this::getEventFromDbOrThrow)
                .collect(Collectors.toSet());*/

        List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
        compilation.setEvents(events);
        Compilation newCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(newCompilation);
    }

    public void deleteCompilation(Long id) {
        getCompilationFromDbOrThrow(id);
        compilationRepository.deleteCompilationById(id);
        log.info("AdminCompilationService: удаление подборки с id={}", id);
    }

    public void deleteEventFromCompilation(Long compId, Long eventId) {
        log.info("AdminCompilationService: удаление события id={} из подборки id={}", eventId, compId);
        getCompilationFromDbOrThrow(compId);
        Compilation compilation = compilationRepository.getReferenceById(compId);
        List<Event> events = compilation.getEvents();
        events.remove(getEventFromDbOrThrow(eventId));
        compilation.setEvents(events);
        compilationRepository.save(compilation);
    }

    public void addEventToCompilation(Long compId, Long eventId) {
        Compilation compilation = getCompilationFromDbOrThrow(compId);
        List<Event> events = compilation.getEvents();
        events.add(getEventFromDbOrThrow(eventId));
        compilation.setEvents(events);
        compilationRepository.save(compilation);
    }

    public void unpinCompilation(Long compId) {
        getCompilationFromDbOrThrow(compId);
        Compilation compilation = compilationRepository.getReferenceById(compId);
        compilation.setPinned(false);
        compilationRepository.save(compilation);
    }

    public void pinCompilation(Long compId) {
        getCompilationFromDbOrThrow(compId);
        Compilation compilation = compilationRepository.getReferenceById(compId);
        compilation.setPinned(true);
        compilationRepository.save(compilation);
    }


/*    private CompilationDto setViewsAndConfirmedRequests(CompilationDto compilationDto) {
        List<EventShortDto> listShortDto = new ArrayList<>();
        for (EventShortDto eventShortDto : compilationDto.getEvents()) {
            EventShortDto shortDto = eventService.setConfirmedRequestsAndViewsEventShortDto(eventShortDto);
            listShortDto.add(shortDto);
        }
        compilationDto.setEvents(listShortDto);
        return compilationDto;
    }*/

    private Compilation getCompilationFromDbOrThrow(Long id) {
        return compilationRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("AdminCompilationService: подборки по id=%d нет в базе", id)));
    }

    private Event getEventFromDbOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("AdminCompilationService: события по id=%d нет в базе", id)));
    }
}
