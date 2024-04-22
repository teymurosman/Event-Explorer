package ru.practicum.mainservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

import static ru.practicum.mainservice.common.Constants.DATE_TIME_PATTERN;

@Data
public class UpdateEventBase {

    private String annotation;

    private Long category;

    private String description;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private String title;
}
