package ru.practicum.main_server.service.public_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.client.StatisticClient;
import ru.practicum.main_server.exception.NotFoundException;
import ru.practicum.main_server.mapper.CompilationMapper;
import ru.practicum.main_server.model.Compilation;
import ru.practicum.main_server.model.dto.CompilationDto;
import ru.practicum.main_server.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PublicCompilationService {
    private final CompilationRepository compilationRepository;
    private final StatisticClient statClient;

    @Autowired
    public PublicCompilationService(CompilationRepository compilationRepository, StatisticClient statClient) {
        this.compilationRepository = compilationRepository;
        this.statClient = statClient;
    }

    public List<CompilationDto> readCompilations(Boolean pinned, int from, int size) {
        log.info("PublicCompilationService: Чтение компиляций pinned={}, from={}, size={}", pinned, from, size);
        if (pinned == null) {
            List<Compilation> comps = compilationRepository.findAll(PageRequest.of(from / size, size)).toList();
            List<CompilationDto> compsWithViews = new ArrayList<>();
            for (Compilation comp : comps) {
                comp.setEvents(statClient.getEventsWithViews(comp.getEvents()));
                compsWithViews.add(CompilationMapper.toCompilationDto(comp));
            }
            return compsWithViews;
        } else {
            List<Compilation> comps = compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size)).toList();
            List<CompilationDto> compsWithViews = new ArrayList<>();
            for (Compilation comp : comps) {
                comp.setEvents(statClient.getEventsWithViews(comp.getEvents()));
                compsWithViews.add(CompilationMapper.toCompilationDto(comp));
            }
            return compsWithViews;
        }
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
