package ru.practicum.main_server.controller.admin_controller;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.main_server.model.dto.NewUserRequest;
import ru.practicum.main_server.model.dto.UserDto;
import ru.practicum.main_server.service.admin_service.AdminUserService;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public UserDto createUser(@RequestBody NewUserRequest newUserRequest) {
        log.info(":::POST /admin/users создание нового пользователя {}", newUserRequest);
        return adminUserService.createUser(newUserRequest);
    }

    @GetMapping
    public List<UserDto> readUsers(
            @RequestParam(required = false) List<Long> ids,
            @PositiveOrZero @RequestParam(defaultValue = "0", required = false) int from,
            @Positive @RequestParam(defaultValue = "10", required = false) int size) {
        log.info(":::GET /users чтение админом пользователей ids={}, from: {}, size: {}", ids, from, size);
        return adminUserService.readUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info(":::DELETE /admin/users/{} удаление пользователя по id", userId);
        adminUserService.deleteUser(userId);
    }
}