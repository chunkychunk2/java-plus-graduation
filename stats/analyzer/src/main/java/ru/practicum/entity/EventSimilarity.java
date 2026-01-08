package ru.practicum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "event_similarities")
public class EventSimilarity {
    @EmbeddedId
    private EventSimilarityId id;

    @NotNull
    @Column(name = "score", nullable = false)
    private Double score;

}