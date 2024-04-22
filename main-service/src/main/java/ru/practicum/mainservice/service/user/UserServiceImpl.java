package ru.practicum.mainservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.NewUserRequest;
import ru.practicum.mainservice.dto.UserDto;
import ru.practicum.mainservice.exception.EntityNotFoundException;
import ru.practicum.mainservice.mapper.UserMapper;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.mainservice.common.PageableFactory.getPageable;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.debug("Получение списка пользователей с парметрами: {}, {}, {}", ids, from, size);

        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(getPageable(from, size)).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAllByIdIn(ids, getPageable(from, size)).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    @Override
    public UserDto add(NewUserRequest newUserRequest) {
        log.debug("Добавление нового пользователя: {}", newUserRequest);

        return userMapper.toUserDto(userRepository.save(userMapper.toUser(newUserRequest)));
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        log.debug("Удаление пользователя с id={}", userId);

        findUserOrThrow(userId);

        userRepository.deleteById(userId);
    }

    private User findUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден."));
    }
}
