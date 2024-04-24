package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class NewCommentDto {

    @NotBlank(message = "Комментарий не может быть пустым.")
    @Size(min = 5, max = 2000, message = "Комментарий должен содержать от 5 до 2000 символов.")
    private String text;
}
