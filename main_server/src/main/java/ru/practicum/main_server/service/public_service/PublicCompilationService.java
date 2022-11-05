package ru.practicum.main_server.service.public_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.client.StatisticClient;
import ru.practicum.main_server.exception.InternalServerErrorException;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.CompilationMapper;
import ru.practicum.main_server.model.Compilation;
import ru.practicum.main_server.model.dto.CompilationDto;
import ru.practicum.main_server.repository.CompilationRepository;
import ru.practicum.main_server.repository.EventRepository;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PublicCompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatisticClient statClient;

    @Autowired
    public PublicCompilationService(CompilationRepository compilationRepository,
                                    EventRepository eventRepository, StatisticClient statClient) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.statClient = statClient;
    }

    public List<CompilationDto> readCompilations(Boolean pinned, int from, int size) {
        log.info("PublicCompilationService: Чтение компиляций pinned={}, from={}, size={}", pinned, from, size);
        if (pinned == null) {
            return compilationRepository.findAll(PageRequest.of(from / size, size)).stream()
                    .map(CompilationMapper::toCompilationDto)
                    .peek(com -> com.getEvents().stream().peek(ev -> ev.setViews(getViews(ev.getId()))))
                    .collect(Collectors.toList());
        } else {
            return compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size))
                    .stream()
                    .map(CompilationMapper::toCompilationDto)
                    .peek(com -> com.getEvents().stream().peek(ev -> ev.setViews(getViews(ev.getId()))))
                    .collect(Collectors.toList());
        }
    }

    private Integer getViews(long eventId) {
        ResponseEntity<Object> responseEntity;
        try {
            responseEntity = statClient.getStats(
                    eventRepository.getReferenceById(eventId).getCreatedOn(),
                    LocalDateTime.now(),
                    List.of("/events/" + eventId),
                    false);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerErrorException("неудачная кодировка");
        }
        if (responseEntity.getStatusCodeValue() < 300) {
            return (Integer) ((LinkedHashMap<?, ?>) Objects.requireNonNull(responseEntity.getBody())).get("hits");
        }
        return 0;
    }

    public CompilationDto readCompilation(long id) {
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(getCompilationFromDbOrThrow(id));
        log.info("PublicCompilationService: Чтение компиляции по id={}", id);
        return compilationDto;
    }

    private Compilation getCompilationFromDbOrThrow(Long id) {
        return compilationRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("AdminCompilationService: подборки по id=%d нет в базе", id)));
    }
}
