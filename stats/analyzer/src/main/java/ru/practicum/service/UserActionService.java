package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.UserAction;
import ru.practicum.entity.UserActionId;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.repository.UserActionRepository;


@Service
@RequiredArgsConstructor
public class UserActionService {

    private final UserActionRepository userActionRepository;

    @Value("${weights.like}")
    private Double likeWeight;
    @Value("${weights.register}")
    private Double registerWeight;
    @Value("${weights.view}")
    private Double viewWeight;

    @Transactional
    public void saveUserAction(UserActionAvro userActionAvro) {
        UserActionId id = UserActionId.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .build();

        UserAction userAction = userActionRepository.findById(id)
                .orElseGet(() -> UserAction.builder().id(id).build());

        Double savedScore = userAction.getUserScore();
        double newScore = getScore(userActionAvro);
        double actualScope = savedScore == null ? newScore : newScore > savedScore ? newScore : savedScore;

        userAction.setUserScore(actualScope);
        userAction.setTimestampAction(userActionAvro.getTimestamp());
        userActionRepository.save(userAction);
    }

    private double getScore(UserActionAvro userActionAvro) {
        return switch (userActionAvro.getType()) {
            case LIKE -> likeWeight;
            case REGISTER -> registerWeight;
            case VIEW -> viewWeight;
        };
    }
}
