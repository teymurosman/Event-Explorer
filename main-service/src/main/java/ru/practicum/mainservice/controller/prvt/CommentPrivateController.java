package ru.practicum.mainservice.controller.prvt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.dto.NewCommentDto;
import ru.practicum.mainservice.service.comment.CommentService;

import javax.validation.Valid;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
public class CommentPrivateController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto add(@PathVariable(name = "userId") Long userId, @PathVariable(name = "eventId") Long eventId,
                          @RequestBody @Valid NewCommentDto newCommentDto) {
        return commentService.add(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable(name = "userId") Long userId, @PathVariable(name = "eventId") Long eventId,
                             @PathVariable(name = "commentId") Long commentId,
                             @RequestBody @Valid NewCommentDto newCommentDto) {
        return commentService.update(userId, eventId, commentId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(name = "userId") Long userId, @PathVariable(name = "eventId") Long eventId,
                       @PathVariable(name = "commentId") Long commentId) {
        commentService.deleteByUser(userId, eventId, commentId);
    }
}
