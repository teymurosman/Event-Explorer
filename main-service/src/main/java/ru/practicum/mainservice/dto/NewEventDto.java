package ru.practicum.mainservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.mainservice.common.Constants.DATE_TIME_PATTERN;

@Data
public class NewEventDto {

    @NotBlank(message = "Краткое описание события не может быть пустым.")
    @Size(min = 20, max = 2000, message = "Краткое описание события должно содержать от 20 до 2000 символов.")
    private String annotation;

    @NotNull(message = "Категория должна быть указана.")
    private Long category;

    @NotBlank(message = "Описание события не может быть пустым.")
    @Size(min = 20, max = 7000, message = "Описание события должно содержать от 20 до 7000 символов.")
    private String description;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime eventDate;

    @NotNull(message = "Локация проведения события должна быть указана.")
    private Location location;

    private boolean paid = false; // default false

    @PositiveOrZero(message = "Количество участников события не может быть отрицательным.")
    private int participantLimit;

    private boolean requestModeration = true; // default true

    @NotBlank(message = "Заголовок события не может быть пустым.")
    @Size(min = 3, max = 120, message = "Заголовок события может содержать от 1 до 120 символов.")
    private String title;
}
