package ru.practicum.main_server.service.public_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.CompilationMapper;
import ru.practicum.main_server.model.dto.CompilationDto;
import ru.practicum.main_server.model.dto.EventShortDto;
import ru.practicum.main_server.repository.CompilationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PublicCompilationService {
    private final CompilationRepository compilationRepository;
    private final PublicEventService eventService;

    @Autowired
    public PublicCompilationService(CompilationRepository compilationRepository,
                                    PublicEventService eventService) {
        this.compilationRepository = compilationRepository;
        this.eventService = eventService;
    }

    public List<CompilationDto> readCompilations(Boolean pinned, int from, int size) {
        log.info("PublicCompilationService: Чтение компиляций pinned={}, from={}, size={}", pinned, from, size);
        if (pinned == null) {
            return compilationRepository.findAll(PageRequest.of(from / size, size)).stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        } else {
            return compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size))
                    .stream()
                    .map(CompilationMapper::toCompilationDto)
                    .map(this::setViewsAndConfirmedRequests)
                    .collect(Collectors.toList());
        }
    }

    public CompilationDto readCompilation(long id) {
        checkCompilationInDb(id);
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilationRepository.getReferenceById(id));
        log.info("PublicCompilationService: Чтение компиляции по id={}", id);
        return setViewsAndConfirmedRequests(compilationDto);
    }

    /**
     * Возвращает подборку с заполненными полями событий views и confirmedRequests
     *
     * @param compilationDto - DTO для подборки
     * @return CompilationDto.class
     */
    private CompilationDto setViewsAndConfirmedRequests(CompilationDto compilationDto) {
        List<EventShortDto> listShortDto = compilationDto.getEvents()
                .stream()
                .map(eventService::setConfirmedRequestsAndViewsEventShortDto)
                .collect(Collectors.toList());
        compilationDto.setEvents(listShortDto);
        return compilationDto;
    }

    private void checkCompilationInDb(long id) {
        if (!compilationRepository.existsById(id)) {
            throw new NotFoundException(String.format("Подборки с id=%d нет в базе", id));
        }
    }
}
