package ru.practicum.exception;

import feign.FeignException;

public class ConflictException extends FeignException {
    public ConflictException(String message) {
        super(409, message);
    }
}
