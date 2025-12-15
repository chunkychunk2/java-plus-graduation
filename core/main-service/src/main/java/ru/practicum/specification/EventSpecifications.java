package ru.practicum.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.entity.Event;
import ru.practicum.entity.EventState;
import ru.practicum.parameters.EventAdminSearchParam;
import ru.practicum.parameters.PublicSearchParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class EventSpecifications {
    public static Specification<Event> userIdIs(List<Long> userIds) {
        return (root, query, criteriaBuilder) -> {
            if (userIds == null || userIds.isEmpty()) {
                return criteriaBuilder.conjunction(); // Всегда истина
            }
            Path<Long> initiatorIdPath = root.get("initiator").get("id");
            return initiatorIdPath.in(userIds);
        };
    }

    public static Specification<Event> categories(List<Long> categories) {
        return (root, query, criteriaBuilder) -> {
            if (categories == null || categories.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Path<Long> categoryIdPath = root.get("category").get("id");
            return categoryIdPath.in(categories);
        };
    }

    public static Specification<Event> states(List<EventState> states) {
        return (root, query, criteriaBuilder) -> {
            if (states == null || states.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Path<String> statePath = root.get("state");
            return statePath.in(states.stream()
                    .map(Enum::name)
                    .collect(Collectors.toList()));
        };
    }

    public static Specification<Event> startAfter(LocalDateTime rangeStart) {
        return (root, query, criteriaBuilder) ->
                rangeStart != null
                        ? criteriaBuilder.greaterThan(root.get("eventDate"), rangeStart)
                        : criteriaBuilder.conjunction();
    }

    public static Specification<Event> startBefore(LocalDateTime rangeEnd) {
        return (root, query, criteriaBuilder) ->
                rangeEnd != null
                        ? criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd)
                        : criteriaBuilder.conjunction();
    }

    public static Specification<Event> isPaid(Boolean paid) {
        return (root, query, criteriaBuilder) -> {
            if (paid == null) {
                return criteriaBuilder.conjunction();
            } else {
                return criteriaBuilder.equal(root.get("paid"), paid);
            }
        };
    }

    public static Specification<Event> textInAnnotationOrDescription(String text) {
        return (root, query, criteriaBuilder) -> {
            if (text == null || text.isBlank()) {
                return criteriaBuilder.conjunction(); // Игнорируем пустой текст
            }

            Predicate annotationPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("annotation")),
                    "%" + text.toLowerCase() + "%"
            );
            Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + text.toLowerCase() + "%"
            );

            return criteriaBuilder.or(annotationPredicate, descriptionPredicate);
        };
    }

    public static Specification<Event> eventAdminSearchParamSpec(EventAdminSearchParam params) {
        return Specification.where(EventSpecifications.userIdIs(params.getUsers()))
                .and(EventSpecifications.states(params.getStates()))
                .and(EventSpecifications.categories(params.getCategories()))
                .and(EventSpecifications.startBefore(params.getRangeEnd()))
                .and(EventSpecifications.startAfter(params.getRangeStart()));
    }

    public static Specification<Event> eventPublicSearchParamSpec(PublicSearchParam params) {
        return Specification.where(EventSpecifications.textInAnnotationOrDescription(params.getText()))
                .and(EventSpecifications.categories(params.getCategories()))
                .and(EventSpecifications.isPaid(params.getPaid()))
                .and(EventSpecifications.startBefore(params.getRangeEnd()))
                .and(EventSpecifications.startAfter(params.getRangeStart()))
                .and(EventSpecifications.states(List.of(EventState.PUBLISHED)));
    }

}
