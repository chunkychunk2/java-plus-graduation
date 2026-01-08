package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.entity.EventSimilarity;
import ru.practicum.entity.EventSimilarityId;

import java.util.List;
import java.util.Set;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarityId> {
    @Query("select es from EventSimilarity es where es.id.firstEvent = :eventId or es.id.secondEvent = :eventId ")
    List<EventSimilarity> findAllByEventId(Long eventId);

    @Query("""
            SELECT es FROM EventSimilarity es
            WHERE es.id.firstEvent IN :userActionsRecentEventIds
              AND es.id.secondEvent NOT IN :allUsersEvents
            UNION
            SELECT es FROM EventSimilarity es \s
            WHERE es.id.secondEvent IN :userActionsRecentEventIds
              AND es.id.firstEvent NOT IN :allUsersEvents
            ORDER BY score DESC
            LIMIT :limit
            """)
    List<EventSimilarity> findSimilarEvents(List<Long> userActionsRecentEventIds, Set<Long> allUsersEvents, int limit);
}