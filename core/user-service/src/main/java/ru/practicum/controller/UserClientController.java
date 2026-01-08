package ru.practicum.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.service.UserService;

@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserClientController {

    private final UserService userService;

    @GetMapping
    public UserDto getUserById(@RequestParam @NotNull @Positive Long userId) {
        log.info("Getting UserDto in UserClientController id={}", userId);
        UserDto userById = userService.getUserById(userId);
        log.info("Returning UserDto in UserClientController dto={}", userById);
        return userById;
    }

    @GetMapping("/short")
    public UserShortDto getUserShortDroById(@RequestParam @NotNull @Positive Long userId) {
        log.info("Getting UserShortDto in UserClientController id={}", userId);
        UserShortDto userShortDtoById = userService.getUserShortDtoById(userId);
        log.info("Returning UserShortDto in UserClientController dto={}", userShortDtoById);
        return userShortDtoById;
    }
}
