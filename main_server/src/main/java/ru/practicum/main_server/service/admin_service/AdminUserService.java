package ru.practicum.main_server.service.admin_service;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.main_server.model.dto.NewUserRequest;
import ru.practicum.main_server.model.dto.UserDto;
import ru.practicum.main_server.mapper.UserMapper;
import ru.practicum.main_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.main_server.mapper.UserMapper.*;

@Slf4j
@Service
@Transactional
public class AdminUserService {
    private final UserRepository userRepository;

    @Autowired
    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> readUsers(List<Long> ids, int from, int size) {
        if (ids.isEmpty()) {
            log.info("AdminUserService: чтение всех пользователей, ids.isEmpty, from: {}, size: {}", from, size);
            return userRepository.findAll(PageRequest.of(from / size, size))
                    .stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
        log.info("AdminUserService: чтение пользователей по ids={}, from: {}, size: {}", ids, from, size);
        return userRepository.findAllByIdIn(ids, PageRequest.of(from / size, size))
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("AdminUserService: создание пользователя с именем {}", newUserRequest.getName());
        return toUserDto(userRepository.save(toUser(newUserRequest)));
    }

    public void deleteUser(long userId) {
        checkUserInDb(userId);
        userRepository.deleteUserById(userId);
        log.info("AdminUserService: пользователь c id={} удален", userId);
    }

    private void checkUserInDb(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(String.format("AdminUserService: пользователя с id=%d нет в базе", userId));
        }
        log.info("AdminUserService: проверка существования пользователя с id={} прошла успешно", userId);
    }
}
