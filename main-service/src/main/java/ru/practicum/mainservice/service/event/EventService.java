package ru.practicum.mainservice.service.event;

import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.model.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getAll(Long userId, int from, int size);

    EventFullDto getById(Long userId, Long eventId);

    EventFullDto add(Long userId, NewEventDto newEventDto);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest updateEvent);

    List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest statusUpdateRequest);

    List<EventFullDto> getAllByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest updateEvent);

    List<EventShortDto> getAll(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                               LocalDateTime rangeEnd, boolean onlyAvailable,
                               String sort, int from, int size,
                               HttpServletRequest httpRequest);

    EventFullDto getById(Long eventId, HttpServletRequest httpRequest);
}
