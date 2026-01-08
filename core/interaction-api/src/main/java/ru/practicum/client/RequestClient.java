package ru.practicum.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "api/v1/request")
public interface RequestClient {

    @PostMapping("/{eventId}/users/{userId}")
    EventRequestStatusUpdateResult updateUsersRequests(@PathVariable Long userId, @PathVariable Long eventId,
                                                       @RequestBody EventRequestStatusUpdateRequest updateRequest) throws FeignException;

    @GetMapping("/{eventId}/users/{userId}")
    List<ParticipationRequestDto> getUsersRequests(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    ;

    @PostMapping("/count")
    Map<Long, Long> countRequestsByEventIdsAndStatus(@RequestBody List<Long> ids, @RequestParam RequestStatus status) throws FeignException;

    @GetMapping("/{eventId}/users/{userId}/exists")
    boolean existsByRequesterAndEventAndStatus(@PathVariable Long userId, @PathVariable Long eventId,
                                               @RequestParam RequestStatus status) throws FeignException;
}
