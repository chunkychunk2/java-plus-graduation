package ru.practicum.exception;

import feign.FeignException;

public class ForbiddenException extends FeignException {
    public ForbiddenException(String message) {
        super(403, message);
    }
}
