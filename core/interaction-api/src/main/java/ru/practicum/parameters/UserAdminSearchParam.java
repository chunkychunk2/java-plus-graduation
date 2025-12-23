package ru.practicum.parameters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class UserAdminSearchParam {
    private List<Long> ids;
    private Integer from;
    private Integer size;

    public Pageable getPageable() {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
