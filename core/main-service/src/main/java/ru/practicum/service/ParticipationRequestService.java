package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final ParticipationRequestMapper requestMapper;

    public List<ParticipationRequestDto> getRequestForEventByUserId(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event id" + eventId + "not found"));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Can't get request for event id=" + eventId + "by user id=" + userId);
        }
        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return requestRepository.findAllByRequesterId(userId)
                .stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in their own event");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event must be published to request participation");
        }

        if (event.getParticipantLimit() != 0 &&
            requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new ConflictException("Event participant limit reached");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setRequester(user);
        request.setEvent(event);

        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED);
        }
        request.setCreated(LocalDateTime.now());

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequests(Long eventId,
                                                         Long userId,
                                                         EventRequestStatusUpdateRequest updateRequest) {
        List<ParticipationRequest> requestList = requestRepository.findAllById(updateRequest.getRequestIds());
        Event event = eventRepository
                .findById(eventId).orElseThrow(() -> new NotFoundException("There is no event id=" + eventId));
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

        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("User is not the requester");
        }

        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    private void updateRequests(List<ParticipationRequest> requests, RequestStatus status, Event event) {
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
                .countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        for (ParticipationRequest request : requests) {
            if (confirmed >= participantLimit) {
                throw new ConflictException("Requests out of limit");
            } else {
                request.setStatus(status);
                confirmed++;
            }
        }
    }
}