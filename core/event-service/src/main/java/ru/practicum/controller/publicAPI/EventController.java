package ru.practicum.controller.publicAPI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.aop.ClientErrorHandler;
import ru.practicum.client.CollectorClient;
import ru.practicum.client.CommentClient;
import ru.practicum.client.RequestClient;
import ru.practicum.dto.comment.CommentWithUserDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.SortSearchParam;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.parameters.PublicSearchParam;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final CommentClient commentClient;
    private final CollectorClient collectorClient;
    private final RequestClient requestClient;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(required = false) SortSearchParam sort,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeEnd can't before rangeStart");
        }
        if (rangeEnd == null && rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("GET /events: text={}, categories={}, paid={}, start={}, end={}, sort={}, from={}, size={}, ip={}, uri={}, ts={}",
                text, categories, paid, rangeStart, rangeEnd, sort, from, size,
                request.getRemoteAddr(), request.getRequestURI(), timestamp);


        PublicSearchParam param = PublicSearchParam.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .onlyAvailable(onlyAvailable)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        List<EventShortDto> events = eventService.searchEvents(param);

        log.info("Returned {} events for GET /events", events.size());
        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable Long id, HttpServletRequest request, @RequestHeader("X-EWM-USER-ID") Long userId) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("GET /events/{}: ip={}, uri={}, ts={}", id, request.getRemoteAddr(), request.getRequestURI(), timestamp);

        collectorClient.collectUserAction(UserActionProto.newBuilder()
                .setEventId(id)
                .setUserId(userId)
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .build());

        EventFullDto event = eventService.getPublishedEventById(id);
        log.info("Returned event {} for GET /events/{}", event.getId(), id);
        return event;
    }

    @ClientErrorHandler
    @GetMapping("/{eventId}/comments")
    public List<CommentWithUserDto> getCommentsByEventId(@PathVariable @Positive Long eventId,
                                                         @RequestParam(defaultValue = "0") Integer from,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        log.info("Returned comments to event id={}", eventId);
        return commentClient.getCommentsByEventId(eventId, from, size);
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendationsForUser(@RequestHeader("X-EWM-USER-ID") Long userId) {
        return eventService.getRecommendationsForUser(userId);
    }

    @PostMapping("/{eventId}/like")
    public void likeEvent(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") Long userId) {
        boolean isConfirmed = requestClient.existsByRequesterAndEventAndStatus(userId, eventId, RequestStatus.CONFIRMED);
        if (isConfirmed) {
            collectorClient.collectUserAction(UserActionProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setActionType(ActionTypeProto.ACTION_LIKE)
                    .build());
        } else {
            throw new BadRequestException("Only participants can like the event");
        }
    }
}
