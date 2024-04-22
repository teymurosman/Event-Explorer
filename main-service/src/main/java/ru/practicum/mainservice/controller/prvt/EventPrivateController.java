package ru.practicum.mainservice.controller.prvt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.service.event.EventService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAll(@PathVariable(name = "userId") Long userId,
                                      @RequestParam(name = "from", defaultValue = "0") int from,
                                      @RequestParam(name = "size", defaultValue = "10") int size) {
        return eventService.getAll(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto add(@PathVariable(name = "userId") Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.add(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable(name = "userId") Long userId,
                                @PathVariable(name = "eventId") Long eventId) {
        return eventService.getById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable(name = "userId") Long userId,
                               @PathVariable(name = "eventId") Long eventId,
                               @RequestBody UpdateEventUserRequest updateEvent) {
        return eventService.updateByUser(userId, eventId, updateEvent);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getParticipationRequests(@PathVariable(name = "userId") Long userId,
                                                                  @PathVariable(name = "eventId") Long eventId) {
        return eventService.getParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable(name = "userId") Long userId,
                                          @PathVariable(name = "eventId") Long eventId,
                                          @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {
        return eventService.updateRequestStatus(userId, eventId, statusUpdateRequest);
    }
}
