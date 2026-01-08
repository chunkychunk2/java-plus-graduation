package ru.practicum.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;

@FeignClient(name = "user-service", path = "api/v1/user")
public interface UserClient {
    @GetMapping
    UserDto getUserById(@RequestParam Long userId) throws FeignException;

    @GetMapping("/short")
    UserShortDto getUserShortDroById(@RequestParam Long userId) throws FeignException;
}
