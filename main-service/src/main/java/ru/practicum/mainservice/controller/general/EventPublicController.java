package ru.practicum.mainservice.controller.general;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.service.event.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.mainservice.common.Constants.DATE_TIME_PATTERN;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAll(@RequestParam(name = "text", required = false) String text,
                                      @RequestParam(name = "categories", required = false) List<Long> categories,
                                      @RequestParam(name = "paid", required = false) Boolean paid,
                                      @RequestParam(name = "rangeStart", required = false)
                                         @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                         LocalDateTime rangeStart,
                                      @RequestParam(name = "rangeEnd", required = false)
                                         @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                         LocalDateTime rangeEnd,
                                      @RequestParam(name = "onlyAvailable", defaultValue = "false")
                                         boolean onlyAvailable,
                                      @RequestParam(name = "sort", required = false) String sort,
                                      @RequestParam(name = "from", defaultValue = "0") int from,
                                      @RequestParam(name = "size", defaultValue = "10") int size,
                                      HttpServletRequest httpRequest) {

        return eventService.getAll(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, httpRequest);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable(name = "eventId") Long eventId, HttpServletRequest httpRequest) {
        return eventService.getById(eventId, httpRequest);
    }
}
