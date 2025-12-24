package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.entity.ParticipationRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    @Query("""
            select r.event, count(r.id)
            from ParticipationRequest r
            where r.event in ?1 and r.status = ?2
            group by r.event""")
    List<Object[]> countRequestsByStatus(List<Long> ids, RequestStatus status);

    long countByEventAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByRequester(Long userId);

    default Map<Long, Long> countRequestsByEventIdsAndStatus(List<Long> ids, RequestStatus status) {
        List<Object[]> result = countRequestsByStatus(ids, status);
        return result.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    List<ParticipationRequest> findAllByEvent(Long eventId);

    boolean existsByRequesterAndEventAndStatus(Long id, Long id1, RequestStatus status);
}