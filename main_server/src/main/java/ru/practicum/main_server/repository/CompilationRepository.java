package ru.practicum.main_server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.main_server.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query("select c from Compilation c where c.pinned = ?1")
    Page<Compilation> findAllByPinned(boolean pinned, Pageable pageable);

    @Modifying
    @Query("delete from Compilation c where c.id = ?1")
    void deleteCompilationById(long id);
}
