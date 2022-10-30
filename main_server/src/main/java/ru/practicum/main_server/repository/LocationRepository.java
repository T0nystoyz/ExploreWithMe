package ru.practicum.main_server.repository;

import ru.practicum.main_server.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
