package ru.practicum.statsserver.service;

import ru.practicum.statsdto.HitCreateRequest;
import ru.practicum.statsdto.StatsResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    void addHit(HitCreateRequest hitCreateRequest);

    List<StatsResponse> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
