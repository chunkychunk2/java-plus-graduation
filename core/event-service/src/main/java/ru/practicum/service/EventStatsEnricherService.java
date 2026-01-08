package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.aop.ClientErrorHandler;
import ru.practicum.client.RequestClient;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.StatsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.request.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class EventStatsEnricherService {

    private final RequestClient requestClient;
    private final StatsClient statsClient;

    @ClientErrorHandler
    public void enrichWithStats(EventFullDto dto) {
        Long eventId = dto.getId();
        Map<Long, Long> views = getViews(List.of(eventId));
        Map<Long, Long> confirmedRequests = requestClient.countRequestsByEventIdsAndStatus(List.of(eventId),
                RequestStatus.CONFIRMED);
        dto.setViews(views.get(eventId) == null ? 0L : views.get(eventId));
        dto.setConfirmedRequests(confirmedRequests.get(eventId) == null ? 0L : confirmedRequests.get(eventId));
    }

    @ClientErrorHandler
    public void enrichWithStatsEventFullDto(List<EventFullDto> dtos) {
        if (dtos.isEmpty()) {
            return;
        }
        List<Long> ids = dtos.stream().map(EventFullDto::getId).toList();
        Map<Long, Long> views = getViews(ids);
        Map<Long, Long> confirmedRequests = requestClient.countRequestsByEventIdsAndStatus(ids,
                RequestStatus.CONFIRMED);
        dtos.forEach(dto -> {
            dto.setConfirmedRequests(confirmedRequests.getOrDefault(dto.getId(), 0L));
            dto.setViews(views.getOrDefault(dto.getId(), 0L));
        });
    }

    @ClientErrorHandler
    public void enrichWithStatsEventShortDto(List<EventShortDto> dtos) {
        if (dtos.isEmpty()) {
            return;
        }
        List<Long> ids = dtos.stream().map(EventShortDto::getId).toList();
        Map<Long, Long> views = getViews(ids);
        Map<Long, Long> confirmedRequests = requestClient.countRequestsByEventIdsAndStatus(ids,
                RequestStatus.CONFIRMED);
        dtos.forEach(dto -> {
            dto.setConfirmedRequests(confirmedRequests.getOrDefault(dto.getId(), 0L));
            dto.setViews(views.getOrDefault(dto.getId(), 0L));
        });
    }

    private Map<Long, Long> getViews(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }

        List<StatsDto> stats = statsClient.getStats(
                "2000-01-01 00:00:00",
                "2100-01-01 00:00:00",
                eventIds.stream().map(id -> "/events/" + id).toList(),
                true);
        return stats.stream()
                .filter(statsDto -> !Objects.equals(statsDto.getUri(), "/events"))
                .collect(toMap(statDto ->
                        Long.parseLong(statDto.getUri().replace("/events/", "")), StatsDto::getHits));
    }
}
