package ru.practicum.exception;

import feign.FeignException;

public class NotFoundException extends FeignException {
    public NotFoundException(String message) {
        super(404, message);
    }
}
