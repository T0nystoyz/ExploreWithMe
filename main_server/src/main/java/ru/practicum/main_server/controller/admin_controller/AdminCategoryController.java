package ru.practicum.main_server.controller.admin_controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.model.dto.CategoryDto;
import ru.practicum.main_server.model.dto.NewCategoryDto;
import ru.practicum.main_server.service.admin_service.AdminCategoryService;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin/categories")
@Slf4j
public class AdminCategoryController {
    private final AdminCategoryService categoryService;

    public AdminCategoryController(AdminCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public CategoryDto create(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info(":::POST /admin/categories создание новой категории {}", newCategoryDto);
        return categoryService.createCategory(newCategoryDto);
    }

    @PatchMapping
    public CategoryDto update(@RequestBody @Valid CategoryDto categoryDto) {
        log.info(":::PATCH /admin/categories обновление категории {}", categoryDto);
        return categoryService.updateCategory(categoryDto);
    }

    @DeleteMapping("/{catId}")
    public void delete(@PathVariable Long catId) {
        log.info(":::DELETE /admin/categories/{} удаление категории по id", catId);
        categoryService.deleteCategory(catId);
    }
}
