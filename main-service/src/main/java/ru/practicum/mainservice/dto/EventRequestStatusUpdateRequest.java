package ru.practicum.mainservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {

    @NotBlank(message = "Список запросов на участие в событии не может быть пустым.")
    private List<@NotNull(message = "Идентификатор запроса не может быть пустым.") Long> requestIds;

    private RequestStatusUpdate status;

    public enum RequestStatusUpdate {
        CONFIRMED, REJECTED
    }
}
