package ru.practicum.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.dto.event.SortSearchParam;

import java.time.LocalDateTime;
import java.util.List;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
public class PublicSearchParam extends PageableParam {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private SortSearchParam sort;

    @Override
    public Pageable getPageable() {
        int page = getFrom() / getSize();
        if (sort == SortSearchParam.EVENT_DATE) {
            return PageRequest.of(page, getSize(), Sort.by("eventDate"));
        } else {
            return PageRequest.of(page, getSize());
        }
    }
}
