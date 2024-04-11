package ru.practicum.statsserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.statsdto.StatsResponse;
import ru.practicum.statsserver.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("select new ru.practicum.statsdto.StatsResponse(eh.app as app, eh.uri, count(eh.ip) as hits) " +
            "from EndpointHit as eh " +
            "where eh.timestamp between :start and :end " +
              "and ((:uris) is null or eh.uri in (:uris))" +
            "group by eh.app, eh.uri " +
            "order by count(eh.ip) desc")
    List<StatsResponse> findAllByTimeStampAndUris(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("uris") List<String> uris);

    @Query("select new ru.practicum.statsdto.StatsResponse(eh.app as app, eh.uri as uri, count(distinct eh.ip) as hits) " +
            "from EndpointHit as eh " +
            "where eh.timestamp between :start and :end " +
            "and ((:uris) is null or eh.uri in (:uris)) " +
            "group by eh.app, eh.uri " +
            "order by count(distinct eh.ip) desc")
    List<StatsResponse> findAllByTimestampAndUrisAndUniqueTrue(@Param("start") LocalDateTime start,
                                                               @Param("end") LocalDateTime end,
                                                               @Param("uris") List<String> uris);
}
