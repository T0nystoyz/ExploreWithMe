package ru.practicum.main_server.repository;

import ru.practicum.main_server.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByIdIn(List<Long> ids, Pageable pageable);


    Page<User> findAll(Pageable pageable);

    @Query("select (count(u) > 0) from User u where u.id = ?1")
    boolean existsById(long userId);

    @Modifying
    @Query("delete from User u where u.id = ?1")
    void deleteUserById(long id);
}
