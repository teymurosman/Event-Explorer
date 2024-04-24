package ru.practicum.mainservice.service.comment;

import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto add(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto update(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto);

    void deleteByUser(Long userId, Long eventId, Long commentId);

    List<CommentDto> getAllByEventId(Long eventId, String sort, int from, int size);

    CommentDto getByIdPublic(Long eventId, Long commentId);

    void deleteByAdmin(Long eventId, Long commentId);
}
