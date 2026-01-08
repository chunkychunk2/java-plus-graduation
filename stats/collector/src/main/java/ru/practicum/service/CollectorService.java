package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CollectorService {

    @Value("${topic.user-actions}")
    private String userActionTopic;

    private final KafkaTemplate<Long, SpecificRecordBase> kafkaTemplate;

    public void sendUserActionEvent(UserActionProto userAction) {
        UserActionAvro actionAvro = getUserActionAvro(userAction);
        kafkaTemplate.send(userActionTopic, actionAvro.getEventId(), actionAvro);
    }

    private UserActionAvro getUserActionAvro(UserActionProto userAction) {
        ActionTypeAvro typeAvro = switch (userAction.getActionType()) {
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            default -> throw new IllegalArgumentException("Action Type is not set");
        };
        return UserActionAvro.newBuilder()
                .setEventId(userAction.getEventId())
                .setUserId(userAction.getUserId())
                .setType(typeAvro)
                .setTimestamp(Instant.ofEpochSecond(userAction.getTimestamp().getSeconds(), userAction.getTimestamp().getNanos()))
                .build();
    }
}
