package ru.practicum.main_server.service.private_service;

import ru.practicum.main_server.model.Location;
import ru.practicum.main_server.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrivateLocationService {
    private final LocationRepository locationRepository;

    public PrivateLocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location save(Location location) {
        return locationRepository.save(location);
    }
}
