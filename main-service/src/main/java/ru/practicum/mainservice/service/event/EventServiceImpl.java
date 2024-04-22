package ru.practicum.mainservice.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.exception.DataConflictException;
import ru.practicum.mainservice.exception.EntityNotFoundException;
import ru.practicum.mainservice.exception.ForbiddenAccessToEntityException;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.mapper.ParticipationRequestMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.ParticipationRequestRepository;
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.mainservice.statsclient.StatsClient;
import ru.practicum.statsdto.StatsResponse;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.mainservice.common.PageableFactory.getPageable;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final ParticipationRequestMapper requestMapper;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getAll(Long userId, int from, int size) {
        log.debug("Получение списка событий добавленных пользователем с id={} с параметрами: {}, {}",
                userId, from, size);

        findUserOrThrow(userId);

        return eventRepository.findAllByInitiatorId(userId, getPageable(from, size)).stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getById(Long userId, Long eventId) {
        log.debug("Получение события с id={} пользователем с id={}", eventId, userId);

        findUserOrThrow(userId);

        return eventMapper.toEventFullDto(findEventOrThrow(eventId));
    }

    @Transactional
    @Override
    public EventFullDto add(Long userId, NewEventDto newEventDto) {
        log.debug("Добавление нового события \"{}\" пользователем с id={}", newEventDto.getTitle(), userId);

        if (!newEventDto.getEventDate().minusHours(2).isAfter(LocalDateTime.now())) {
            throw new ValidationException("Дата и время проведения события не может быть раньше, " +
                    "чем через два часа от текущего момента.");
        }
        Event event = eventMapper.toEvent(newEventDto);

        event.setInitiator(findUserOrThrow(userId));
        event.setCategory(findCategoryOrThrow(newEventDto.getCategory()));

        event.setCreatedOn(LocalDateTime.now());

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Transactional
    @Override
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        log.debug("Обновление события с id={} от пользователя с id={}", eventId, userId);

        Event event = findEventOrThrow(eventId);

        LocalDateTime newEventDate = updateEvent.getEventDate();
        if (newEventDate != null) { // Вложенные if, чтобы не бросалось исключении при null полях
            if (!newEventDate.minusHours(2).isAfter(LocalDateTime.now())) {
                throw new ValidationException("Дата и время проведения события не может быть раньше, " +
                        "чем через два часа от текущего момента.");
            } else {
                event.setEventDate(newEventDate);
            }
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new DataConflictException("Изменить можно только отмененные события " +
                    "или события в состоянии ожидания модерации.");
        }

        validateAndUpdateEvent(updateEvent, event);

        findUserOrThrow(userId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenAccessToEntityException("Невозможно изменить чужое событие.");
        }

        if (updateEvent.getStateAction() != null) {
            switch (updateEvent.getStateAction()) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                default:
                    throw new ValidationException("Неверно указано изменение состояния события.");
            }
        }

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId) {
        log.debug("Получение информации о запросах на участие в событии с id={} пользователя с id={}", eventId, userId);

        findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenAccessToEntityException("Только создатель события может получить информацию " +
                    "о запросах на участие.");
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.debug("Изменение статуса заявок на участие в событии с id={} пользователя с id={}", eventId, userId);

        findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenAccessToEntityException("Только создатель события может изменять статусы заявок.");
        }

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            return null;
        }

        if (event.getConfirmedRequests() == event.getParticipantLimit()) {
            throw new DataConflictException("Достигнут лимит по заявкам на данное событие.");
        }

        List<ParticipationRequest> requests =
                requestRepository.findAllByIdInAndEventIdEquals(statusUpdateRequest.getRequestIds(), eventId);
        boolean hasNotPending = requests.stream()
                .anyMatch(request -> request.getStatus() != ParticipationRequestStatus.PENDING);
        if (hasNotPending) {
            throw new DataConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания.");
        }

        switch (statusUpdateRequest.getStatus()) {
            case CONFIRMED:
                return updateRequestStatusConfirmed(event, requests);
            case REJECTED:
                return updateRequestStatusRejected(requests);
            default:
                throw new ValidationException("Неверно указан новый статус для запросов на участие в событии.");
        }
    }

    @Override
    public List<EventFullDto> getAllByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        log.debug("Получение списка событий администратором.");

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала поиска не может быть позже даты конца поиска.");
        }
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }
        rangeStart = rangeStart == null ? LocalDateTime.now().minusYears(100) : rangeStart;
        rangeEnd = rangeEnd == null ? LocalDateTime.now().plusYears(100) : rangeEnd;


        return eventRepository.findAllAdmin(users, states, categories, rangeStart, rangeEnd, getPageable(from, size))
                .stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        log.debug("Обновление события с id={} администратором", eventId);

        Event event = findEventOrThrow(eventId);

        if (updateEvent.getEventDate() != null) {
            if (updateEvent.getEventDate().minusHours(1).isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата начала изменяемого события должна быть не ранее," +
                        " чем за час от даты публикации.");
            }
        }

        validateAndUpdateEvent(updateEvent, event);

        if (updateEvent.getStateAction() != null) {
            switch (updateEvent.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new DataConflictException("Событие можно публиковать, " +
                                "только если оно в состоянии ожидания публикации.");
                    }
                    event.setState(EventState.PUBLISHED);
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new DataConflictException("Событие можно отклонить, " +
                                "только если оно еще не опубликовано.");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ValidationException("Неверно указано действие.");
            }
        }

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getAll(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd, boolean onlyAvailable, String sort, int from, int size,
                                      HttpServletRequest httpRequest) {
        log.debug("Получение списка событий публичным запросом");

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала поиска не может быть позже даты конца поиска.");
        }
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }
        rangeStart = rangeStart == null ? LocalDateTime.now().minusYears(100) : rangeStart;
        rangeEnd = rangeEnd == null ? LocalDateTime.now().plusYears(100) : rangeEnd;

        List<Event> events = eventRepository.findAllPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, getPageable(from, size));

        Map<String, Long> uriToIdMap = events.stream()
                .map(Event::getId)
                .collect(Collectors.toMap(eventId -> "/events/" + eventId, eventId -> eventId));
        List<String> urisForViews = List.copyOf(uriToIdMap.keySet());
        List<StatsResponse> viewsList = statsClient.getStats(rangeStart, rangeEnd,
                urisForViews, true);
        Map<Long, Long> views = viewsList.stream()
                .collect(Collectors
                        .toMap(statsResponse -> uriToIdMap.get(statsResponse.getUri()), StatsResponse::getHits));

        List<EventShortDto> eventShortDtos = events.stream()
                .map(eventMapper::toEventShortDto)
                .peek(eventShortDto
                        -> eventShortDto.setViews(views.getOrDefault(eventShortDto.getId(), 0L)))
                .collect(Collectors.toList());

        if (sort != null) {
            switch (sort) {
                case "EVENT_DATE":
                    eventShortDtos.sort(Comparator.comparing(EventShortDto::getEventDate).reversed());
                    break;
                case "VIEWS":
                    eventShortDtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
                    break;
                default:
                    throw new ValidationException("Параметр сортировки указан неверно.");
            }
        }

        statsClient.addHit(httpRequest);

        return eventShortDtos;
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest httpRequest) {
        log.debug("Получение события с id={}", eventId);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id=" + eventId + " не найдено."));

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);

        LocalDateTime start = LocalDateTime.now().minusYears(100);
        LocalDateTime end = LocalDateTime.now().plusYears(100);
        List<StatsResponse> stats = statsClient.getStats(start, end, List.of("/events/" + eventId), true);
        if (stats != null && !stats.isEmpty()) {
            eventFullDto.setViews(stats.get(0).getHits());
        }

        statsClient.addHit(httpRequest);


        return eventFullDto;
    }

    private User findUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id=" + userId + " не найден."));
    }

    private Event findEventOrThrow(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id=" + eventId + " не найдено."));
    }

    private Category findCategoryOrThrow(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Категория с id=" + catId + " не найдена."));
    }

    private void validateAndUpdateEvent(UpdateEventBase updateEvent, Event event) {

        String annotation = updateEvent.getAnnotation();
        if (annotation != null) {
            if (annotation.length() >= 20 && annotation.length() <= 2000) {
                event.setAnnotation(annotation);
            } else {
                throw new ValidationException("Краткое описание события должно содержать от 20 до 2000 символов.");
            }
        }

        String description = updateEvent.getDescription();
        if (description != null) {
            if (description.length() >= 20 && description.length() <= 7000) {
                event.setDescription(description);
            } else {
                throw new ValidationException("Описание события должно содержать от 20 до 7000 символов.");
            }
        }

        Location location = updateEvent.getLocation();
        if (location != null) {
            event.setLocation(location);
        }

        Boolean paid = updateEvent.getPaid();
        if (paid != null) {
            event.setPaid(paid);
        }

        Integer participantLimit = updateEvent.getParticipantLimit();
        if (participantLimit != null) {
            if (participantLimit >= 0) {
                event.setParticipantLimit(participantLimit);
            } else {
                throw new ValidationException("Количество участников события не может быть отрицательным.");
            }
        }

        String title = updateEvent.getTitle();
        if (title != null) {
            if (title.length() >= 3 && title.length() <= 120) {
                event.setTitle(title);
            } else {
                throw new ValidationException("Заголовок события может содержать от 1 до 120 символов.");
            }
        }

        if (updateEvent.getCategory() != null) {
            findCategoryOrThrow(updateEvent.getCategory());
            if (updateEvent.getCategory() != null) {
                event.setCategory(findCategoryOrThrow(updateEvent.getCategory()));
            }
        }
    }

    private EventRequestStatusUpdateResult updateRequestStatusConfirmed(Event event, List<ParticipationRequest> requests) {
        int availableForConfirmation = event.getParticipantLimit() - event.getConfirmedRequests();
        if (requests.size() > availableForConfirmation) {
            List<ParticipationRequest> underLimitRequests = requests.subList(0, availableForConfirmation);
            underLimitRequests.forEach(request -> request.setStatus(ParticipationRequestStatus.CONFIRMED));
            event.setConfirmedRequests(event.getConfirmedRequests() + underLimitRequests.size()); // Прибавить событию подтвержденные заявки
            eventRepository.save(event);

            List<ParticipationRequest> overLimitRequests =
                    requests.subList(availableForConfirmation + 1, requests.size());
            overLimitRequests.forEach(request -> request.setStatus(ParticipationRequestStatus.REJECTED));

            requestRepository.saveAll(requests); // 1 обращение к бд
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(requestMapper.toListParticipationRequestDto(underLimitRequests))
                    .rejectedRequests(requestMapper.toListParticipationRequestDto(overLimitRequests))
                    .build();
        } else {
            requests.forEach(request -> request.setStatus(ParticipationRequestStatus.CONFIRMED));
            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size()); // Прибавить событию подтвержденные заявки
            eventRepository.save(event);

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(requestMapper
                            .toListParticipationRequestDto(requestRepository.saveAll(requests)))
                    .build();
        }
    }

    private EventRequestStatusUpdateResult updateRequestStatusRejected(List<ParticipationRequest> requests) {
        requests.forEach(request -> request.setStatus(ParticipationRequestStatus.REJECTED));

        return EventRequestStatusUpdateResult.builder()
                .rejectedRequests(requestMapper
                        .toListParticipationRequestDto(requestRepository.saveAll(requests)))
                .build();
    }
}
