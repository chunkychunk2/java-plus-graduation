package ru.practicum.controller.publicAPI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.comment.CommentWithUserDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.SortSearchParam;
import ru.practicum.exception.BadRequestException;
import ru.practicum.parameters.PageableSearchParam;
import ru.practicum.parameters.PublicSearchParam;
import ru.practicum.service.CommentService;
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
    private final CommentService commentService;
    private final StatsClient statsClient;

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

        statsClient.postHit(HitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(timestamp)
                .build());

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
    public EventFullDto getEventById(@PathVariable Long id, HttpServletRequest request) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("GET /events/{}: ip={}, uri={}, ts={}", id, request.getRemoteAddr(), request.getRequestURI(), timestamp);

        statsClient.postHit(HitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(timestamp)
                .build());

        EventFullDto event = eventService.getEventById(id);
        log.info("Returned event {} for GET /events/{}", event.getId(), id);
        return event;
    }

    @GetMapping("/{eventId}/comments")
    public List<CommentWithUserDto> getCommentsByEventId(@PathVariable @Positive Long eventId,
                                                         @RequestParam(defaultValue = "0") Integer from,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        PageableSearchParam param = PageableSearchParam.builder().size(size).from(from).build();
        log.info("Returned comments to event id={}", eventId);
        return commentService.getCommentsByEventId(eventId, param.getPageable());
    }
}
