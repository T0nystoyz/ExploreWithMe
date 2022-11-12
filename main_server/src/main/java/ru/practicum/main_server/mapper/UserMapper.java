package ru.practicum.main_server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.main_server.model.User;
import ru.practicum.main_server.model.dto.NewUserRequest;
import ru.practicum.main_server.model.dto.UserShortDto;
import ru.practicum.main_server.model.dto.UserDto;

@UtilityClass
public class UserMapper {
    public static UserDto toUserDto(User user) {
        return UserDto
                .builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto
                .builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static User toUser(NewUserRequest newUserRequest) {
        return User
                .builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
    }
}
