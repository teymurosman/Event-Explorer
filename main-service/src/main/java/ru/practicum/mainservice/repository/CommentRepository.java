package ru.practicum.mainservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mainservice.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndAuthorIdAndEventId(Long commentId, Long authorId, Long eventId);

    Optional<Comment> findByIdAndEventId(Long commentId, Long eventId);

    List<Comment> findByEventId(Long eventId, Pageable page);
}
