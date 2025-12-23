package ru.practicum.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShotCommentDto;

@FeignClient(name = "event-service", path = "api/v1/event")
public interface EventClient {
    @GetMapping("/{eventId}")
    EventFullDto getEventById(@PathVariable Long eventId) throws FeignException;

    @GetMapping("/{eventId}/comment")
    EventShotCommentDto getEventShotCommentDtoById(@PathVariable Long eventId) throws FeignException;
}
