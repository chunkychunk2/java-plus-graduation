package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.entity.UserAction;
import ru.practicum.entity.UserActionId;

import java.util.List;
import java.util.Set;

public interface UserActionRepository extends JpaRepository<UserAction, UserActionId> {

    @Query("select ua.id.eventId from UserAction ua where ua.id.userId = :userId")
    Set<Long> findAllEventIdsByIdUserId(Long userId);

    @Query("SELECT ua.id.eventId FROM UserAction ua WHERE ua.id.userId = :userId ORDER BY ua.timestampAction DESC")
    List<Long> findEventIdsFromRecentActionsByUserId(Long userId, Pageable pageable);

    @Query("select ua from UserAction ua where ua.id.eventId in :eventIds")
    List<UserAction> findAllByIdEventIds(List<Long> eventIds);

    @Query("""
            select ua.id.eventId, sum(ua.userScore)
            from UserAction ua
            where ua.id.eventId in :eventIds
            group by ua.id.eventId
            """)
    List<Object[]> findInteractions(List<Long> eventIds);
}