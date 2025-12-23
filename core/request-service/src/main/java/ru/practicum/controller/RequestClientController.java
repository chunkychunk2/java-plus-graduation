package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/request")
public class RequestClientController {

    private final ParticipationRequestService requestService;

    @PostMapping("/{eventId}/users/{userId}")
    public EventRequestStatusUpdateResult updateUsersRequests(@PathVariable @Positive Long userId,
                                                              @PathVariable @Positive Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Updating event in RequestClientController userId={}, eventId={}, update request={}",
                userId, eventId, updateRequest);
        return requestService.updateRequests(userId, eventId, updateRequest);
    }

    @GetMapping("/{eventId}/users/{userId}")
    public List<ParticipationRequestDto> getUsersRequests(@PathVariable @Positive Long userId,
                                                          @PathVariable @Positive Long eventId) {
        log.info("Getting users requests in RequestClientController userId={}, eventId={}", userId, eventId);
        return requestService.getRequestForEventByUserId(userId, eventId);
    }

    @PostMapping("/count")
    public Map<Long, Long> countRequestsByEventIdsAndStatus(@RequestBody List<Long> ids,
                                                            @RequestParam RequestStatus status) {
        log.info("Counting request in RequestClientController ids={}, status={}", ids, status);
        return requestService.countRequestsByEventIdsAndStatus(ids, status);
    }

    @GetMapping("/{eventId}/users/{userId}/exists")
    public boolean existsByRequesterAndEventAndStatus(@PathVariable @Positive Long userId,
                                                      @PathVariable @Positive Long eventId,
                                                      @RequestParam RequestStatus status) {
        log.info("Look if request exists in RequestClientController userId={}, eventId={}, status={}", userId, eventId, status);
        return requestService.existsByRequesterAndEventAndStatus(userId, eventId, status);
    }
}
