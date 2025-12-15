package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;
    private final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public void postHit(@RequestBody HitDto hitDto) {
        log.info("POST /hit: {}", hitDto);
        statsService.saveHit(hitDto);
    }

    @GetMapping("/stats")
    public List<StatsDto> getStats(@RequestParam @DateTimeFormat(pattern = dateTimeFormat) LocalDateTime start,
                                   @RequestParam @DateTimeFormat(pattern = dateTimeFormat) LocalDateTime end,
                                   @RequestParam(required = false) List<String> uris,
                                   @RequestParam(defaultValue = "false") boolean unique) {
        log.info("GET /stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start param can't be before end");
        }
        return statsService.getStats(start, end, uris, unique);
    }
}
