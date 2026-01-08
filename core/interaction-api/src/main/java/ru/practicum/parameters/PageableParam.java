package ru.practicum.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class PageableParam {
    protected Integer from;
    protected Integer size;

    public Pageable getPageable() {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}