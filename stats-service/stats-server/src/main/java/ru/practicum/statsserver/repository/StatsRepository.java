package ru.practicum.statsserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.statsdto.StatsResponse;
import ru.practicum.statsserver.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT new ru.practicum.statsdto.StatsResponse(eh.app as app, eh.uri, count(eh.ip) AS hits)\n" +
            "FROM EndpointHit AS eh\n" +
            "WHERE eh.timestamp BETWEEN :start AND :end\n" +
              "AND ((:uris) IS NULL OR eh.uri IN (:uris))\n" +
            "GROUP BY eh.app, eh.uri\n" +
            "ORDER BY COUNT(eh.ip) DESC")
    List<StatsResponse> findAllByTimeStampAndUris(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.statsdto.StatsResponse(eh.app as app, eh.uri as uri, COUNT(DISTINCT eh.ip) AS hits)\n" +
            "FROM EndpointHit AS eh\n" +
            "WHERE eh.timestamp BETWEEN :start AND :end\n" +
            "and ((:uris) IS NULL OR eh.uri IN (:uris))\n" +
            "GROUP BY eh.app, eh.uri\n" +
            "ORDER BY COUNT(DISTINCT eh.ip) DESC")
    List<StatsResponse> findAllByTimestampAndUrisAndUniqueTrue(@Param("start") LocalDateTime start,
                                                               @Param("end") LocalDateTime end,
                                                               @Param("uris") List<String> uris);
}
