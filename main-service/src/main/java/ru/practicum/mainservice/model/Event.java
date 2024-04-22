package ru.practicum.mainservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.mainservice.dto.Location;

import javax.persistence.*;
import java.time.LocalDateTime;

import static ru.practicum.mainservice.common.Constants.DATE_TIME_PATTERN;

@Data
@Entity
@Table(name = "events")
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annotation", length = 2000, nullable = false)
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "confirmed_requests")
    private int confirmedRequests;

    @Column(name = "created_on", nullable = false)
    @JsonFormat(pattern = DATE_TIME_PATTERN, shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdOn;

    @Column(name = "description", length = 7000, nullable = false)
    private String description;

    @Column(name = "event_date", nullable = false)
    @JsonFormat(pattern = DATE_TIME_PATTERN, shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "latitude", nullable = false)),
            @AttributeOverride(name = "lon", column = @Column(name = "longitude", nullable = false))
    })
    private Location location;

    @Column(name = "paid", nullable = false)
    private boolean paid;

    @Column(name = "participant_limit", nullable = false)
    private int participantLimit;

    @Column(name = "published_on")
    @JsonFormat(pattern = DATE_TIME_PATTERN, shape = JsonFormat.Shape.STRING)
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation", nullable = false)
    private boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private EventState state = EventState.PENDING;

    @Column(name = "title", length = 120, nullable = false)
    private String title;
}
