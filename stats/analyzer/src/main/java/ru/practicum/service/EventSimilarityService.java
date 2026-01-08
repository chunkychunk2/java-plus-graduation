package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.EventSimilarity;
import ru.practicum.entity.EventSimilarityId;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.repository.EventSimilarityRepository;


@Service
@RequiredArgsConstructor
public class EventSimilarityService {

    private final EventSimilarityRepository eventSimilarityRepository;

    @Transactional
    public void saveEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        EventSimilarityId id = EventSimilarityId.builder()
                .firstEvent(eventSimilarityAvro.getEventA())
                .secondEvent(eventSimilarityAvro.getEventB())
                .build();

        EventSimilarity eventSimilarity = eventSimilarityRepository.findById(id)
                .orElseGet(() -> EventSimilarity.builder().id(id).build());

        eventSimilarity.setScore(eventSimilarityAvro.getScore());
        eventSimilarityRepository.save(eventSimilarity);
    }
}
