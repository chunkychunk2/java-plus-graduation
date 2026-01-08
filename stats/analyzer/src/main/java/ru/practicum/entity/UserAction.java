package ru.practicum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_actions")
public class UserAction {
    @EmbeddedId
    private UserActionId id;

    @NotNull
    @Column(name = "user_score", nullable = false)
    private Double userScore;

    @NotNull
    @Column(name = "timestamp_action", nullable = false)
    private Instant timestampAction;

}