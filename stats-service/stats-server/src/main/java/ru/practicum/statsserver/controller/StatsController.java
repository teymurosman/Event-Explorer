package ru.practicum.statsserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsdto.HitCreateRequest;
import ru.practicum.statsdto.StatsResponse;
import ru.practicum.statsserver.exception.ValidationException;
import ru.practicum.statsserver.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<HttpStatus> addHit(@RequestBody @Valid HitCreateRequest hitCreateRequest) {
        statsService.addHit(hitCreateRequest);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/stats")
    public List<StatsResponse> getStats(@RequestParam(name = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                            LocalDateTime start,
                                        @RequestParam(name = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                            LocalDateTime end,
                                        @RequestParam(name = "uris", required = false) List<String> uris,
                                        @RequestParam(name = "unique", defaultValue = "false") boolean unique) {
        validateTime(start, end);
        return statsService.getStats(start, end, uris, unique);
    }

    private void validateTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ValidationException("Дата начала должна быть раньше даты окончания.");
        }
    }
}
