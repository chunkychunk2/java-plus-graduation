package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.CollectorClient;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class ParticipationRequestPrivateController {

    private final ParticipationRequestService requestService;
    private final CollectorClient collectorClient;

    @GetMapping
    public List<ParticipationRequestDto> getRequests(@PathVariable Long userId) {
        return requestService.getRequestsByUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        collectorClient.collectUserAction(UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_REGISTER)
                .build());
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}
