package ru.practicum.main_server.service.public_service;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.main_server.mapper.CategoryMapper;
import ru.practicum.main_server.model.Category;
import ru.practicum.main_server.model.dto.CategoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.main_server.repository.CategoryRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.main_server.mapper.CategoryMapper.*;

@Slf4j
@Service
public class PublicCategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public PublicCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> readAllCategories(int from, int size) {
        log.info("PublicCategoryService: чтение всех категорий from: {}, size: {}", from, size);
        return categoryRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto readCategory(long id) {
        log.info("PublicCategoryService: чтение категории по id={}", id);
        return toCategoryDto(getCategoryFromDbOrThrow(id));
    }

    private Category getCategoryFromDbOrThrow(long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String
                .format("PublicCategoryService: категории с id=%d нет в базе", id)));
    }
}
