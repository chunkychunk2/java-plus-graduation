package ru.practicum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class EventSimilarityId implements Serializable {
    private static final long serialVersionUID = 4682851433840970069L;
    @NotNull
    @Column(name = "first_event", nullable = false)
    private Long firstEvent;

    @NotNull
    @Column(name = "second_event", nullable = false)
    private Long secondEvent;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EventSimilarityId entity = (EventSimilarityId) o;
        return Objects.equals(this.secondEvent, entity.secondEvent) &&
               Objects.equals(this.firstEvent, entity.firstEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secondEvent, firstEvent);
    }

}