package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.User;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.parameters.UserAdminSearchParam;
import ru.practicum.repository.UserRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Transactional
    public UserDto create(NewUserRequest user) {
        User saved = userRepository.save(mapper.toEntity(user));
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        userRepository.deleteById(userId);
    }

    public List<UserDto> getUsers(UserAdminSearchParam params) {
        Page<User> users;
        if (params.getIds() != null) {
            users = userRepository.getUsersByIdIn(params.getIds(), params.getPageable());
        } else {
            users = userRepository.findAll(params.getPageable());
        }
        return users.stream()
                .map(mapper::toDto)
                .toList();
    }
}
