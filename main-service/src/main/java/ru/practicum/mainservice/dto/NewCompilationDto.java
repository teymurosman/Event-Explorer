package ru.practicum.mainservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class NewCompilationDto {

    private Set<Long> events;

    private boolean pinned = false;

    @NotBlank(message = "Заголовок подборки не может быть пустым.")
    @Size(min = 1, max = 50, message = "Заголовок подборки должен содержать от 1 до 50 символов.")
    private String title;
}
