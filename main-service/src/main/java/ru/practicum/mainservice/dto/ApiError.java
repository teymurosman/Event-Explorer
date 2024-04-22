package ru.practicum.mainservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.mainservice.common.Constants.DATE_TIME_PATTERN;

@Getter
@Setter
@Builder
public class ApiError {

    private HttpStatus status;

    private String reason;

    private String message;

    @Builder.Default
    private String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
}
