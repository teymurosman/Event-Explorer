package ru.practicum.mainservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static ru.practicum.mainservice.common.Constants.DATE_TIME_PATTERN;

@Getter
@Setter
public class CommentDto {

    private Long id;

    private Long eventId;

    private String text;

    private String authorName;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime createdOn;
}
