package ru.practicum.mainservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static ru.practicum.mainservice.common.Constants.DATE_TIME_PATTERN;

@Getter
@Setter
@Builder
@Entity
@Table(name = "participation_requests")
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created", nullable = false)
    @JsonFormat(pattern = DATE_TIME_PATTERN, shape = JsonFormat.Shape.STRING)
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id")
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParticipationRequestStatus status = ParticipationRequestStatus.PENDING;
}
