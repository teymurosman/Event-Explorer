package ru.practicum.mainservice.controller.prvt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.service.participation.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestPrivateController {

    private final ParticipationRequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getRequests(@PathVariable(name = "userId") Long userId) {
        return requestService.getRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto add(@PathVariable(name = "userId") Long userId,
                                       @RequestParam(name = "eventId") Long eventId) {
        return requestService.add(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable(name = "userId") Long userId,
                                          @PathVariable(name = "requestId") Long requestId) {
        return requestService.cancel(userId, requestId);
    }
}
