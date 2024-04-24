package ru.practicum.mainservice.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.dto.NewCommentDto;
import ru.practicum.mainservice.exception.DataConflictException;
import ru.practicum.mainservice.exception.EntityNotFoundException;
import ru.practicum.mainservice.mapper.CommentMapper;
import ru.practicum.mainservice.model.Comment;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.repository.CommentRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.mainservice.common.PageableFactory.getPageable;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CommentDto add(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.debug("Добавление комментария пользователем с id={} к событию с id={}", userId, eventId);

        Comment comment = commentMapper.toComment(newCommentDto);

        User author = findUserOrThrow(userId);
        comment.setAuthor(author);

        Event event = findEventOrThrow(eventId);
        comment.setEvent(event);

        comment.setCreatedOn(LocalDateTime.now());

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public CommentDto update(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto) {
        log.debug("Обновление комментария commentId={}, userId={}, eventId={}", commentId, userId, eventId);

        Comment comment = findCommentOrThrow(commentId, userId, eventId);

        // В бд время лежит без TZ, а LocalDateTime.now() возвращает системное
        ZonedDateTime createdOnUtc = comment.getCreatedOn().atZone(ZoneId.of("UTC"));
        ZonedDateTime currentUtc = LocalDateTime.now().atZone(ZoneId.of("UTC"));
        if (!createdOnUtc.isBefore(currentUtc.plusMinutes(20))) {
            throw new DataConflictException("Обновление комментария возможно только " +
                    "в течение 20 минут после его создания.");
        }
        comment.setText(newCommentDto.getText());

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public void deleteByUser(Long userId, Long eventId, Long commentId) {
        log.debug("Удаление комментария commentId={}, userId={}, eventId={}", commentId, userId, eventId);

        findCommentOrThrow(commentId, userId, eventId);

        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getAllByEventId(Long eventId, String sort, int from, int size) {
        log.debug("Получение списка комментариев события с id={}", eventId);

        findEventOrThrow(eventId);

        Sort sortByCreated;
        switch (sort) {
            case "asc":
            case "ASC":
                sortByCreated = Sort.by(Sort.Direction.ASC, "createdOn");
                break;
            case "desc":
            case "DESC":
                sortByCreated = Sort.by(Sort.Direction.DESC, "createdOn");
                break;
            default:
                throw new ValidationException("Неверно указан параметр сортировки.");
        }

        return commentRepository.findByEventId(eventId, getPageable(from, size, sortByCreated)).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getByIdPublic(Long eventId, Long commentId) {
        log.debug("Получение комментария с id={} к событию с id={}", commentId, eventId);

        return commentMapper.toCommentDto(findCommentOrThrow(commentId, eventId));
    }

    @Transactional
    @Override
    public void deleteByAdmin(Long eventId, Long commentId) {
        log.debug("Удаление администратором комментария с id={} к событию с id={}", commentId, eventId);

        findCommentOrThrow(commentId, eventId);

        commentRepository.deleteById(commentId);
    }

    private Comment findCommentOrThrow(long commentId, long eventId) { // Поиск + валидация по событию
        return commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id=" + commentId + " не найден."));
    }

    private Comment findCommentOrThrow(Long commentId, Long userId, Long eventId) { // Поиск + валидация по автору и событию
        return commentRepository.findByIdAndAuthorIdAndEventId(commentId, userId, eventId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id=" + commentId + " не найден."));
    }

    private User findUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден."));
    }

    private Event findEventOrThrow(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id=" + eventId + " не найдено."));
    }
}
