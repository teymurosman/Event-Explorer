package ru.practicum.mainservice.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.service.comment.CommentService;

@RestController
@RequestMapping("/admin/events/{eventId}/comments")
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(name = "eventId") Long eventId, @PathVariable(name = "commentId") Long commentId) {
        commentService.deleteByAdmin(eventId, commentId);
    }
}
