package ru.practicum.statsserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statsdto.HitCreateRequest;
import ru.practicum.statsdto.StatsResponse;
import ru.practicum.statsserver.mapper.StatsMapper;
import ru.practicum.statsserver.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public void addHit(HitCreateRequest hitCreateRequest) {
        log.info("Добавление нового посещения: {}", hitCreateRequest);

        statsRepository.save(statsMapper.toEndpointHit(hitCreateRequest));
    }

    @Override
    public List<StatsResponse> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Получение статистики с параметрами start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        if (unique) {
            return statsRepository.findAllByTimestampAndUrisAndUniqueTrue(start, end, uris);
        } else {
            return statsRepository.findAllByTimeStampAndUris(start, end, uris);
        }
    }
}
