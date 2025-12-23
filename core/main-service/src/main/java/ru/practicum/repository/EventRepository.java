package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.entity.Event;
import ru.practicum.entity.EventState;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @EntityGraph(attributePaths = {"initiator", "category"})
    Page<Event> findByInitiatorId(Long id, Pageable pageable);

    @EntityGraph(attributePaths = {"initiator", "category"})
    Optional<Event> findByIdAndState(Long id, EventState state);

    @EntityGraph(attributePaths = {"initiator", "category"})
    Page<Event> findAll(Specification<Event> specification, Pageable pageable);
}
