package ru.practicum.mainservice.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class NewUserRequest {

    @NotNull(message = "Адрес электронной почты не может быть пустым.")
    @Size(min = 6, max = 254, message = "Адрес электронной почты должен содержать от 6 до 254 символов.")
    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Неверный формат электронной почты.")
    private String email;

    @NotBlank(message = "Имя пользователя не может быть пустым.")
    @Size(min = 2, max = 250, message = "Имя пользователя должно содержать от 2 до 250 символов.")
    private String name;
}
