package ru.practicum.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@SuperBuilder
@Getter
public class UserAdminSearchParam extends PageableParam {
    private List<Long> ids;
}