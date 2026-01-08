package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.EventSimilarity;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserActionRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationsService {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;


    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        long userId = request.getUserId();
        int maxResult = request.getMaxResult();

        List<Long> userActionsRecentEventIds = userActionRepository.findEventIdsFromRecentActionsByUserId(userId, PageRequest.of(0, maxResult));
        if (userActionsRecentEventIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> allUsersEvents = userActionRepository.findAllEventIdsByIdUserId(userId);

        List<EventSimilarity> similarEvents = eventSimilarityRepository.findSimilarEvents(userActionsRecentEventIds, allUsersEvents, maxResult);
        return similarEvents.stream()
                .map(es -> RecommendedEventProto.newBuilder()
                        .setEventId(allUsersEvents.contains(es.getId().getFirstEvent()) ? es.getId().getSecondEvent()
                                : es.getId().getFirstEvent())
                        .setScore(es.getScore())
                        .build())
                .toList();
    }

    public List<RecommendedEventProto> getSimilarEvent(SimilarEventsRequestProto request) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResult = request.getMaxResult();

        List<EventSimilarity> eventSimilarities = eventSimilarityRepository.findAllByEventId(eventId);
        Set<Long> userActionSet = userActionRepository.findAllEventIdsByIdUserId(userId);

        return eventSimilarities.stream()
                .map(es -> {
                    long similarEventId = es.getId().getFirstEvent() == eventId ?
                            es.getId().getSecondEvent() : es.getId().getFirstEvent();
                    return Map.entry(similarEventId, es.getScore());
                })
                .filter(entry -> !userActionSet.contains(entry.getKey()))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(maxResult)
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Object[]> interactions = userActionRepository.findInteractions(request.getEventIdList());
        if (interactions == null || interactions.isEmpty()) {
            return Collections.emptyList();
        } else {
            return interactions.stream().map(el -> RecommendedEventProto.newBuilder()
                    .setEventId((long) el[0])
                    .setScore((double) el[1])
                    .build()).toList();
        }
    }
}
