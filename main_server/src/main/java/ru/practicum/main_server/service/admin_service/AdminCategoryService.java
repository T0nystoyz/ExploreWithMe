package ru.practicum.main_server.service.admin_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.model.Category;
import ru.practicum.main_server.model.dto.CategoryDto;
import ru.practicum.main_server.model.dto.NewCategoryDto;
import ru.practicum.main_server.repository.CategoryRepository;

import javax.persistence.EntityNotFoundException;

import static ru.practicum.main_server.mapper.CategoryMapper.*;
import static ru.practicum.main_server.mapper.CategoryMapper.toCategoryFromCategoryDto;

@Slf4j
@Service
@Transactional
public class AdminCategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public AdminCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = toCategoryFromNewCategoryDto(newCategoryDto);
        log.info("AdminCategoryService: создание категории с именем {} прошло успешно", newCategoryDto.getName());
        return toCategoryDto(categoryRepository.save(category));
    }

    public CategoryDto updateCategory(CategoryDto categoryDto) {
        log.info("AdminCategoryService: обновление категории {}", categoryDto);
        categoryRepository.getReferenceById(categoryDto.getId());
        categoryRepository.save(toCategoryFromCategoryDto(categoryDto));
        return toCategoryDto(categoryRepository.save(toCategoryFromCategoryDto(categoryDto)));
    }

    public void deleteCategory(long id) {
        log.info("AdminCategoryService: удаление категории по id={}", id);
        checkCategoryInDb(id);
        categoryRepository.deleteById(id);
    }

    private void checkCategoryInDb(long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format("CategoryService: категории с id=%d нет в базе", id));
        }
        log.info("AdminCategoryService: проверка существования категории с id={} прошла успешно", id);
    }
}
