package ru.practicum.mainservice.exception;

public class ForbiddenAccessToEntityException extends RuntimeException {
    public ForbiddenAccessToEntityException(String message) {
        super(message);
    }
}
