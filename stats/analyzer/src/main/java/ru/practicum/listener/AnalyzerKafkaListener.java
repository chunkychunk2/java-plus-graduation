package ru.practicum.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.EventSimilarityService;
import ru.practicum.service.UserActionService;


@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerKafkaListener {

    private final UserActionService userActionService;
    private final EventSimilarityService eventSimilarityService;

    @KafkaListener(
            topics = "${topic.user-actions}",
            containerFactory = "userActionAvroConcurrentKafkaListenerContainerFactory")
    public void handleUserActions(UserActionAvro userActionAvro) {
        log.info("Handle user action {}", userActionAvro);
        userActionService.saveUserAction(userActionAvro);
    }

    @KafkaListener(
            topics = "${topic.events-similarity}",
            containerFactory = "eventSimilarityAvroConcurrentKafkaListenerContainerFactory")
    public void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Handle event similarity {}", eventSimilarityAvro);
        eventSimilarityService.saveEventSimilarity(eventSimilarityAvro);
    }

}
