package ru.practicum.service;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimilarityCalculator {
    /**
     * Матрица весов действий пользователей c мероприятиями в виде отображения
     * Map<EventID,<Map<UserId, MaxWeight>>
     */
    final Map<Long, Map<Long, Double>> eventToUserToMaxWeight = new ConcurrentHashMap<>();

    /**
     * Общие суммы весов каждого из мероприятий, где ключ — мероприятие, а значение — сумма весов действий пользователей с ним
     * Map<EventId, WeightSum>
     */
    final Map<Long, Double> eventWeightSum = new ConcurrentHashMap<>();

    /**
     * Сумма минимальных весов для каждой пары мероприятий. Тогда ключом будет одно из мероприятий, а значением —
     * ещё одно отображение, где ключ — второе мероприятие, а значение — сумма их минимальных весов
     * Map<EventId_A, Map<EventId_B, MinWeight>>
     * где, всегда EventId_A < EventId_B
     */
    final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();


    public List<EventSimilarityAvro> calculateSimilarity(UserActionAvro userActionAvro) {
        long userId = userActionAvro.getUserId();
        long eventId = userActionAvro.getEventId();
        double newWeight = getWeight(userActionAvro.getType());

        Map<Long, Double> userWeights = eventToUserToMaxWeight.get(eventId);
        Double oldWeight = userWeights != null ? userWeights.get(userId) : null;

        if (oldWeight == null || newWeight > oldWeight) {
            // обновляем базовые структуры
            updateBasicStructures(userId, eventId, newWeight, oldWeight);

            // вычисляем similarity для случаев если событие новое или уже есть в мапах
            if (oldWeight == null) {
                return similarityForNewEvent(userId, eventId, newWeight);
            } else {
                return similarityForOldEvent(userId, eventId, newWeight, oldWeight);
            }
        } else {
            return Collections.emptyList();
        }
    }

    private void updateBasicStructures(long userId, long eventId, double newWeight, Double oldWeight) {
        // обновляем eventToUserToMaxWeight
        Map<Long, Double> userWeights = eventToUserToMaxWeight
                .computeIfAbsent(eventId, k -> new ConcurrentHashMap<>());
        userWeights.put(userId, newWeight);

        // обновляем eventWeightSum
        double delta = (oldWeight != null) ? (newWeight - oldWeight) : newWeight;
        eventWeightSum.merge(eventId, delta, Double::sum);
    }

    private List<EventSimilarityAvro> similarityForNewEvent(long userId, long eventId, double newWeight) {
        List<Long> eventIds = userActivityList(userId, eventId);

        // добавляем вклады во все minWeightsSums
        for (Long otherEventId : eventIds) {
            Double otherWeight = eventToUserToMaxWeight.get(otherEventId).get(userId);
            double minWeight = Math.min(newWeight, otherWeight);

            addToMinWeights(eventId, otherEventId, minWeight);
        }

        // вычисляем similarity для всех пар
        return eventIds.stream()
                .map(otherEventId -> similarity(eventId, otherEventId))
                .toList();
    }

    private List<EventSimilarityAvro> similarityForOldEvent(long userId, long eventId, Double newWeight, Double oldWeight) {
        List<Long> eventIds = userActivityList(userId, eventId);

        // обновляем minWeightsSums для всех пар, где изменился минимальный вес
        for (Long otherEventId : eventIds) {
            Double otherWeight = eventToUserToMaxWeight.get(otherEventId).get(userId);

            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);
            double deltaMin = newMin - oldMin;

            if (deltaMin != 0) {
                addToMinWeights(eventId, otherEventId, deltaMin);
            }
        }


        return eventIds.stream()
                .map(otherEventId -> similarity(eventId, otherEventId))
                .toList();
    }

    private List<Long> userActivityList(long userId, long excludeEventId) {
        return eventToUserToMaxWeight.entrySet().stream()
                .filter(entry -> {
                    Long eventId = entry.getKey();
                    Map<Long, Double> users = entry.getValue();
                    return !eventId.equals(excludeEventId) &&
                           users != null &&
                           users.containsKey(userId);
                })
                .map(Map.Entry::getKey)
                .toList();
    }

    private EventSimilarityAvro similarity(long eventA, long eventB) {
        return EventSimilarityAvro.newBuilder()
                .setEventA(Math.min(eventA, eventB))
                .setEventB(Math.max(eventA, eventB))
                .setScore(calculateSimilarity(eventA, eventB))
                .setTimestamp(Instant.now())
                .build();
    }

    private Double calculateSimilarity(long eventA, long eventB) {
        Double sA = eventWeightSum.get(eventA);
        Double sB = eventWeightSum.get(eventB);

        Long first = Math.min(eventA, eventB);
        Long second = Math.max(eventA, eventB);
        Double sMin = minWeightsSums.getOrDefault(first, Collections.emptyMap())
                .getOrDefault(second, 0.0);

        if (sA != null && sB != null && sA > 0 && sB > 0 && sMin != null) {
            return sMin / (Math.sqrt(sA) * Math.sqrt(sB));
        } else {
            return 0.0;
        }
    }

    private void addToMinWeights(long eventA, long eventB, double value) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .merge(second, value, Double::sum);
    }

    private double getWeight(ActionTypeAvro type) {
        return switch (type) {
            case LIKE -> 1.0;
            case REGISTER -> 0.8;
            case VIEW -> 0.4;
        };
    }
}
