package ru.practicum.mainservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class NewCategoryDto {

    @NotBlank(message = "Название категории не может быть пустым.")
    @Size(min = 1, max = 50, message = "Название категории должно содержать от 1 до 50 символов.")
    private String name;
}
