package ru.practicum.mainservice.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.UpdateEventAdminRequest;
import ru.practicum.mainservice.model.EventState;
import ru.practicum.mainservice.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.mainservice.common.Constants.DATE_TIME_PATTERN;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getAllByAdmin(@RequestParam(name = "users", required = false) List<Long> users,
                                            @RequestParam(name = "states", required = false) List<EventState> states,
                                            @RequestParam(name = "categories", required = false) List<Long> categories,
                                            @RequestParam(name = "rangeStart", required = false)
                                                @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                                LocalDateTime rangeStart,
                                            @RequestParam(name = "rangeEnd", required = false)
                                                @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                                LocalDateTime rangeEnd,
                                            @RequestParam(name = "from", defaultValue = "0") int from,
                                            @RequestParam(name = "size", defaultValue = "10") int size) {
        return eventService.getAllByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable(name = "eventId") Long eventId,
                                      @RequestBody UpdateEventAdminRequest updateEvent) {
        return eventService.updateByAdmin(eventId, updateEvent);
    }
}
