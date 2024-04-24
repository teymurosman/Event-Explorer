package ru.practicum.mainservice.controller.general;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.service.comment.CommentService;

import java.util.List;

@RestController
@RequestMapping("events/{eventId}/comments")
@RequiredArgsConstructor
public class CommentPublicController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getAllByEventId(@PathVariable(name = "eventId") Long eventId,
                                            @RequestParam(name = "sort", defaultValue = "ASC") String sort,
                                            @RequestParam(name = "from", defaultValue = "0") int from,
                                            @RequestParam(name = "size", defaultValue = "10") int size) {
        return commentService.getAllByEventId(eventId, sort, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getById(@PathVariable(name = "eventId") Long eventId,
                              @PathVariable(name = "commentId") Long commentId) {
        return commentService.getByIdPublic(eventId, commentId);
    }
}
