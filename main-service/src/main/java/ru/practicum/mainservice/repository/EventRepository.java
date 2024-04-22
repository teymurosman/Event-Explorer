package ru.practicum.mainservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByCategoryId(Long catId);

    Optional<Event> findByIdAndState(Long id, EventState state);

    List<Event> findAllByInitiatorId(Long initiatorId, Pageable page);

@Query("SELECT e\n" +
        "FROM Event e\n" +
        "WHERE (:initiators IS NULL OR e.initiator.id IN :initiators)\n" +
        "AND (:states IS NULL OR e.state IN :states)\n" +
        "AND (:categories IS NULL OR e.category.id IN :categories)\n" +
        "AND (e.eventDate > :rangeStart)\n" +
        "AND (e.eventDate < :rangeEnd)")
    List<Event> findAllAdmin(@Param("initiators") List<Long> initiators, @Param("states") List<EventState> states,
                             @Param("categories") List<Long> categories,
                             @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd,
                             Pageable page);

@Query("SELECT e\n" +
        "FROM Event e\n" +
        "WHERE e.state = 'PUBLISHED'\n" +
        "AND (:text IS NULL OR ((LOWER(e.annotation) LIKE CONCAT('%', LOWER(:text), '%'))\n" +
        "OR (LOWER(e.description) LIKE CONCAT('%', LOWER(:text), '%'))))\n" +
        "AND (:categories IS NULL OR e.category.id IN :categories)\n" +
        "AND (:paid IS NULL OR e.paid = :paid)\n" +
        "AND (e.eventDate > :rangeStart) AND (e.eventDate < :rangeEnd)\n" +
        "AND (:onlyAvailable = false OR (:onlyAvailable = true AND (e.participantLimit > " +
        "(SELECT COUNT(*)\n" +
        "FROM ParticipationRequest AS pr\n" +
        "WHERE e.id = pr.event.id\n" +
        "AND pr.status = 'CONFIRMED')\n" +
        "OR (e.participantLimit = 0 )))) ")
    List<Event> findAllPublic(@Param("text") String text, @Param("categories") List<Long> categories,
                              @Param("paid") Boolean paid, @Param("rangeStart") LocalDateTime rangeStart,
                              @Param("rangeEnd") LocalDateTime rangeEnd, @Param("onlyAvailable") boolean onlyAvailable,
                              Pageable page);
}
