package ru.practicum.statsdto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class HitCreateRequest {

    @NotBlank(message = "Имя сервиса не может быть пустым.")
    private String app;

    @NotBlank(message = "URI запроса не может быть пустым.")
    private String uri;

    @NotBlank(message = "IP-адрес пользователя не может быть пустым.")
    private String ip;

    @NotNull(message = "Дата запроса не может быть пустой.")
    private String timestamp;
}
