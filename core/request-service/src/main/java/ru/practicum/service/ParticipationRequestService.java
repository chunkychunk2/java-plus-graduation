package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.aop.ClientErrorHandler;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.entity.ParticipationRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventClient eventClient;
    private final UserClient userClient;

    private final ParticipationRequestMapper requestMapper;

    @ClientErrorHandler
    public List<ParticipationRequestDto> getRequestForEventByUserId(Long userId, Long eventId) {
        EventFullDto eventById = eventClient.getEventById(eventId);
        if (!Objects.equals(eventById.getInitiator().getId(), userId)) {
            throw new ConflictException("Can't get request for event id=" + eventId + "by user id=" + userId);
        }
        List<ParticipationRequest> requests = requestRepository.findAllByEvent(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @ClientErrorHandler
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        userClient.getUserById(userId);
        return requestRepository.findAllByRequester(userId)
                .stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Transactional
    @ClientErrorHandler
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        userClient.getUserById(userId);

        EventFullDto event = eventClient.getEventById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in their own event");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event must be published to request participation");
        }

        if (event.getParticipantLimit() != 0 &&
            requestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new ConflictException("Event participant limit reached");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setRequester(userId);
        request.setEvent(eventId);

        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED);
        }
        request.setCreated(LocalDateTime.now());

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Transactional
    @ClientErrorHandler
    public EventRequestStatusUpdateResult updateRequests(Long userId,
                                                         Long eventId,
                                                         EventRequestStatusUpdateRequest updateRequest) {
        List<ParticipationRequest> requestList = requestRepository.findAllById(updateRequest.getRequestIds());
        EventFullDto event = eventClient.getEventById(eventId);
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Can't update event id=" + eventId + " requests by user id=" + userId);
        }
        updateRequests(requestList, updateRequest.getStatus(), event);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        requestList.forEach(request -> {
            switch (request.getStatus()) {
                case RequestStatus.REJECTED -> result.getRejectedRequests().add(requestMapper.toDto(request));
                case RequestStatus.CONFIRMED -> result.getConfirmedRequests().add(requestMapper.toDto(request));
            }
        });
        return result;
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getRequester().equals(userId)) {
            throw new ConflictException("User is not the requester");
        }

        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    private void updateRequests(List<ParticipationRequest> requests, RequestStatus status, EventFullDto event) {
        boolean hasNotPendingRequests = requests.stream().map(ParticipationRequest::getStatus).anyMatch(el -> el != RequestStatus.PENDING);
        if (hasNotPendingRequests)
            throw new ConflictException("Can't change status when request status is not PENDING");

        if (status == RequestStatus.REJECTED) {
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestStatus.REJECTED);
            }
            return;
        }
        Boolean requestModeration = event.getRequestModeration();
        Integer participantLimit = event.getParticipantLimit();

        if (!requestModeration && participantLimit == null) {
            requests.forEach(request -> request.setStatus(status));
            return;
        }

        long confirmed = requestRepository
                .countByEventAndStatus(event.getId(), RequestStatus.CONFIRMED);
        for (ParticipationRequest request : requests) {
            if (confirmed >= participantLimit) {
                throw new ConflictException("Requests out of limit");
            } else {
                request.setStatus(status);
                confirmed++;
            }
        }
    }

    public Map<Long, Long> countRequestsByEventIdsAndStatus(List<Long> ids, RequestStatus status) {
        return requestRepository.countRequestsByEventIdsAndStatus(ids, status);
    }

    public boolean existsByRequesterAndEventAndStatus(Long userId, Long eventId, RequestStatus status) {
        return requestRepository.existsByRequesterAndEventAndStatus(userId, eventId, status);
    }
}