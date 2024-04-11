package ru.practicum.statsdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class HitCreateRequest {

    @NotBlank(message = "Имя сервиса не может быть пустым.")
    private String app;

    @NotBlank(message = "URI запроса не может быть пустым.")
    private String uri;

    @NotBlank(message = "IP-адрес пользователя не может быть пустым.")
    private String ip;

    @NotNull(message = "Дата запроса не может быть пустой.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
