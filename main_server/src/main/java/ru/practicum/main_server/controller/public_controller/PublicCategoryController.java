package ru.practicum.main_server.controller.public_controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.model.dto.CategoryDto;
import ru.practicum.main_server.service.public_service.PublicCategoryService;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@Slf4j
public class PublicCategoryController {
    private final PublicCategoryService categoryService;

    public PublicCategoryController(PublicCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    List<CategoryDto> readCategories(@RequestParam(defaultValue = "0") int from,
                                     @RequestParam(defaultValue = "10") int size) {
        log.info(":::GET /categories чтение списка категорий from={}, size={}", from, size);
        return categoryService.readAllCategories(from, size);
    }

    @GetMapping("/{id}")
    CategoryDto readCategory(@PathVariable long id) {
        log.info(":::GET /categories/{} чтение категории по id", id);
        return categoryService.readCategory(id);
    }
}
