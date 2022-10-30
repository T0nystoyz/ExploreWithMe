package ru.practicum.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main_server.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findAll(Pageable pageable);

    Category findCategoryById(long catId);

    Category findById(long catId);
}
