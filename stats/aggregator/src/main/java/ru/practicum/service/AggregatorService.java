package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorService {

    private final SimilarityCalculator calculator;
    private final KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate;
    @Value("${topic.events-similarity}")
    private String similarityTopic;

    @KafkaListener(
            topics = "${topic.user-actions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(UserActionAvro userActionAvro) {
        log.info("Get new action to proceed {}", userActionAvro);
        List<EventSimilarityAvro> similarities = calculator.calculateSimilarity(userActionAvro);

        similarities.forEach(similarity -> {
            log.info("Sending similarity={}", similarity);
            kafkaTemplate.send(similarityTopic, similarity);
        });
    }

}
