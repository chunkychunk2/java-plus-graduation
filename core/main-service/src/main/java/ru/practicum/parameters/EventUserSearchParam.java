package ru.practicum.parameters;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@SuperBuilder
@Getter
public class EventUserSearchParam extends PageableParam {
    private Long userId;
}

