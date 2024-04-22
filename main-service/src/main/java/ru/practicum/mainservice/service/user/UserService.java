package ru.practicum.mainservice.service.user;

import ru.practicum.mainservice.dto.NewUserRequest;
import ru.practicum.mainservice.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    UserDto add(NewUserRequest newUserRequest);

    void delete(Long userId);
}
