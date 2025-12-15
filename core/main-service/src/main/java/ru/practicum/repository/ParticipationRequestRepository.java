package ru.practicum.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.entity.ParticipationRequest;
import ru.practicum.entity.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    @Query("""
            select r.event.id, count(r.id)
            from ParticipationRequest r
            where r.event.id in ?1 and r.status = ?2
            group by r.event.id""")
    List<Object[]> countRequestsByStatus(List<Long> ids, RequestStatus status);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @EntityGraph(attributePaths = {"requester", "event"})
    List<ParticipationRequest> findAllByRequesterId(Long userId);

    default Map<Long, Long> countRequestsByEventIdsAndStatus(List<Long> ids, RequestStatus status) {
        List<Object[]> result = countRequestsByStatus(ids, status);
        return result.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    @EntityGraph(attributePaths = {"requester", "event"})
    List<ParticipationRequest> findAllByEventId(Long eventId);

    boolean existsByRequester_IdAndEvent_IdAndStatus(Long id, Long id1, RequestStatus status);
}