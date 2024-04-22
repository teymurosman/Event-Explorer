package ru.practicum.mainservice.service.participation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.exception.DataConflictException;
import ru.practicum.mainservice.exception.EntityNotFoundException;
import ru.practicum.mainservice.exception.ForbiddenAccessToEntityException;
import ru.practicum.mainservice.mapper.ParticipationRequestMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.ParticipationRequestRepository;
import ru.practicum.mainservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final ParticipationRequestMapper requestMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {
        log.debug("Получение списка запросов на участие в событиях пользователя с id={}", userId);

        findUserOrThrow(userId);

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ParticipationRequestDto add(Long userId, Long eventId) {
        log.debug("Добавление нового запроса на участие в события с id]{} от пользователя с id={}", eventId, userId);

        User requester = findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new DataConflictException("Можно отправить запрос на участие только в опубликованном событии.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException("Нельзя отправить запрос на участие в собственном событии.");
        }
        if (requestRepository.findByRequesterIdAndEventId(userId, event.getId()).isPresent()) {
            throw new DataConflictException("Нельзя отправлять повторные запросы на участие в событии.");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= event.getConfirmedRequests()) {
            throw new DataConflictException("У события достигнут лимит запросов на участие.");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .build();
        if (event.isRequestModeration() && event.getParticipantLimit() != 0) {
            request.setStatus(ParticipationRequestStatus.PENDING);
        } else {
            request.setStatus(ParticipationRequestStatus.CONFIRMED); // Если не нужна пре-модерация или без лимита - confirmed
            event.setConfirmedRequests(event.getConfirmedRequests() + 1); // Добавить событию confirmedRequests
            eventRepository.save(event);
        }

        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        log.debug("Отмена запроса на участие в событие с id={} пользователем с id={}", requestId, userId);

        findUserOrThrow(userId);

        ParticipationRequest request = findParticipationRequestOrThrow(requestId);
        if (!request.getRequester().getId().equals(userId)) {
            throw new ForbiddenAccessToEntityException("Нельзя отменить чужой запрос.");
        }

        if (request.getStatus() == ParticipationRequestStatus.CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1); // Убираем 1 confirmed
            eventRepository.save(event);
        }
        request.setStatus(ParticipationRequestStatus.CANCELED);

        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    private User findUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден."));
    }

    private Event findEventOrThrow(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id=" + eventId + " не найдено."));
    }

    private ParticipationRequest findParticipationRequestOrThrow(long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос на участие с id=" + requestId + " не найден."));
    }
}
