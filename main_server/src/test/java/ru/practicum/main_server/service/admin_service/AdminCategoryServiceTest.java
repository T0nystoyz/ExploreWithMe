package ru.practicum.main_server.service.admin_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.model.dto.CategoryDto;
import ru.practicum.main_server.model.dto.NewCategoryDto;
import ru.practicum.main_server.repository.CategoryRepository;
import ru.practicum.main_server.service.public_service.PublicCategoryService;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AdminCategoryServiceTest {
    @Autowired
    CategoryRepository repo;
    @Autowired
    PublicCategoryService pubService;
    @Autowired
    AdminCategoryService admService;
    @Test
    void delete() {
        CategoryDto cat = admService.createCategory(new NewCategoryDto("UNNAMED"));
        CategoryDto cat2 = admService.createCategory(new NewCategoryDto("UNNAMED2"));
        admService.deleteCategory(cat.getId());
        assertEquals(cat2, pubService.readCategory(cat2.getId()));
        assertThrows(EntityNotFoundException.class, () -> pubService.readCategory(cat.getId()));
    }
}