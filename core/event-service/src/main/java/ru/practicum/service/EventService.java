package ru.practicum.service;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.aop.ClientErrorHandler;
import ru.practicum.client.RequestClient;
import ru.practicum.client.StatsClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.StatsDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.parameters.EventAdminSearchParam;
import ru.practicum.parameters.EventUserSearchParam;
import ru.practicum.parameters.PublicSearchParam;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static ru.practicum.specification.EventSpecifications.eventAdminSearchParamSpec;
import static ru.practicum.specification.EventSpecifications.eventPublicSearchParamSpec;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final RequestClient requestClient;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    private final UserClient userClient;

    public List<EventShortDto> getUsersEvents(EventUserSearchParam params) {
        Page<Event> events = eventRepository.findByInitiator(params.getUserId(), params.getPageable());

        List<EventShortDto> result = events.stream()
                .map(eventMapper::toShortDto)
                .toList();
        enrichWithStatsEventShortDto(result);
        return result;

    }

    @Transactional
    public EventFullDto saveEvent(NewEventDto dto, Long userId) {
        //check if user exist
        userClient.getUserShortDroById(userId);
        Event saved = eventRepository.saveAndFlush(eventMapper.toEntity(dto, userId));
        EventFullDto fullDto = eventMapper.toFullDto(saved);
        fullDto.setViews(0L);
        fullDto.setConfirmedRequests(0L);
        return fullDto;
    }

    public List<EventShortDto> searchEvents(PublicSearchParam param) {

        Page<Event> events = eventRepository.findAll(eventPublicSearchParamSpec(param), param.getPageable());
        Map<Long, Event> eventsMap = events.stream().collect(toMap(Event::getId, Function.identity()));

        List<EventShortDto> eventShortDtos = events.stream()
                .map(eventMapper::toShortDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        enrichWithStatsEventShortDto(eventShortDtos);

        if (param.getOnlyAvailable()) {
            eventShortDtos = eventShortDtos.stream()
                    .filter(dto -> dto.getConfirmedRequests() >= eventsMap.get(dto.getId()).getParticipantLimit())
                    .collect(Collectors.toList());
        }
        if (param.getSort() == SortSearchParam.VIEWS) {
            eventShortDtos.sort(Comparator.comparingLong(EventShortDto::getViews));
        }
        return eventShortDtos;
    }

    public EventFullDto getPublishedEventById(Long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или не опубликовано"));
        EventFullDto dto = eventMapper.toFullDto(event);
        enrichWithStats(dto);
        return dto;
    }

    public EventFullDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие id" + id + "не найдено"));
        EventFullDto dto = eventMapper.toFullDto(event);
        enrichWithStats(dto);
        return dto;
    }

    public EventFullDto getEventByIdAndUserId(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!Objects.equals(event.getInitiator(), userId)) {
            throw new ConflictException("Событие добавленно не теущем пользователем");
        }

        EventFullDto dto = eventMapper.toFullDto(event);
        enrichWithStats(dto);
        return dto;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EventFullDto updateEventByUser(Long eventId, Long userId, UpdateEventUserRequest event) {
        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено id=" + eventId));
        if (!Objects.equals(eventToUpdate.getInitiator(), userId) ||
            eventToUpdate.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Событие добавленно не теущем пользователем или уже было опубликовано");
        }
        updateNouNullFields(eventToUpdate, event);
        if (event.getStateAction() == UserEventAction.CANCEL_REVIEW) {
            eventToUpdate.setState(EventState.CANCELED);
        } else if (event.getStateAction() == UserEventAction.SEND_TO_REVIEW) {
            eventToUpdate.setState(EventState.PENDING);
        }

        Event updated = eventRepository.save(eventToUpdate);

        EventFullDto result = eventMapper.toFullDto(updated);
        enrichWithStats(result);
        return result;
    }

    public List<EventFullDto> getEventsByParams(EventAdminSearchParam params) {
        Page<Event> searched = eventRepository.findAll(eventAdminSearchParamSpec(params), params.getPageable());

        List<EventFullDto> result = searched.stream()
                .limit(params.getSize())
                .map(eventMapper::toFullDto)
                .toList();
        enrichWithStatsEventFullDto(result);
        return result;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event id=" + eventId + "not found"));
        if (event.getState() != EventState.PENDING && updateRequest.getStateAction() == AdminEventAction.PUBLISH_EVENT) {
            throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
        }
        if (event.getState() == EventState.PUBLISHED && updateRequest.getStateAction() == AdminEventAction.REJECT_EVENT) {
            throw new ConflictException("Cannot reject the event because it's not in the right state: PUBLISHED");
        }
        if (event.getEventDate().minusHours(1).isBefore(LocalDateTime.now())) {
            throw new ConflictException("To late to change event");
        }
        updateNouNullFields(event, updateRequest);
        event.setState(updateRequest.getStateAction() == AdminEventAction.PUBLISH_EVENT ? EventState.PUBLISHED : EventState.CANCELED);
        if (event.getState() == EventState.PUBLISHED &&
            updateRequest.getStateAction() == AdminEventAction.PUBLISH_EVENT) {
            event.setPublishedOn(LocalDateTime.now());
        }
        Event updated = eventRepository.save(event);

        EventFullDto dto = eventMapper.toFullDto(updated);
        enrichWithStats(dto);

        return dto;
    }

    public EventShotCommentDto getEventShotCommentDtoById(@Positive Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event id=" + eventId + "not found"));
        return eventMapper.toEventShotCommentDto(event);
    }

    @ClientErrorHandler
    private void enrichWithStats(EventFullDto dto) {
        Long eventId = dto.getId();
        Map<Long, Long> views = getViews(List.of(eventId));
        Map<Long, Long> confirmedRequests = requestClient.countRequestsByEventIdsAndStatus(List.of(eventId),
                RequestStatus.CONFIRMED);
        dto.setViews(views.get(eventId));
        dto.setConfirmedRequests(confirmedRequests.get(eventId));
    }

    @ClientErrorHandler
    private void enrichWithStatsEventFullDto(List<EventFullDto> dtos) {
        List<Long> ids = dtos.stream().map(EventFullDto::getId).toList();
        Map<Long, Long> views = getViews(ids);
        Map<Long, Long> confirmedRequests = requestClient.countRequestsByEventIdsAndStatus(ids,
                RequestStatus.CONFIRMED);
        dtos.forEach(dto -> {
            dto.setConfirmedRequests(confirmedRequests.get(dto.getId()) == null ? 0 : confirmedRequests.get(dto.getId()));
            dto.setViews(views.get(dto.getId()) == null ? 0 : views.get(dto.getId()));
        });
    }

    @ClientErrorHandler
    private void enrichWithStatsEventShortDto(List<EventShortDto> dtos) {
        List<Long> ids = dtos.stream().map(EventShortDto::getId).toList();
        Map<Long, Long> views = getViews(ids);
        Map<Long, Long> confirmedRequests = requestClient.countRequestsByEventIdsAndStatus(ids,
                RequestStatus.CONFIRMED);
        dtos.forEach(dto -> {
            dto.setConfirmedRequests(confirmedRequests.get(dto.getId()) == null ? 0 : confirmedRequests.get(dto.getId()));
            dto.setViews(views.get(dto.getId()) == null ? 0 : views.get(dto.getId()));
        });
    }

    private void updateNouNullFields(Event eventToUpdate, UpdateEventRequest event) {
        if (event.getAnnotation() != null) eventToUpdate.setAnnotation(event.getAnnotation());
        if (event.getCategory() != null) eventToUpdate.setCategory(Category.builder().id(event.getCategory()).build());
        if (event.getDescription() != null) eventToUpdate.setDescription(event.getDescription());
        if (event.getEventDate() != null) eventToUpdate.setEventDate(event.getEventDate());
        if (event.getLocation() != null) {
            eventToUpdate.setLat(event.getLocation().getLat());
            eventToUpdate.setLon(event.getLocation().getLon());
        }
        if (event.getPaid() != null) eventToUpdate.setPaid(event.getPaid());
        if (event.getParticipantLimit() != null) eventToUpdate.setParticipantLimit(event.getParticipantLimit());
        if (event.getRequestModeration() != null) eventToUpdate.setRequestModeration(event.getRequestModeration());
        if (event.getTitle() != null) eventToUpdate.setTitle(event.getTitle());
    }

    /**
     * Getting stats from stats client
     */
    private Map<Long, Long> getViews(List<Long> eventIds) {
        List<StatsDto> stats = statsClient.getStats(
                "2000-01-01 00:00:00",
                "2100-01-01 00:00:00",
                eventIds.stream().map(id -> "/events/" + id).toList(),
                true);
        return stats.stream()
                .filter(statsDto -> !statsDto.getUri().equals("/events"))
                .collect(toMap(statDto ->
                        Long.parseLong(statDto.getUri().replace("/events/", "")), StatsDto::getHits));
    }
}
