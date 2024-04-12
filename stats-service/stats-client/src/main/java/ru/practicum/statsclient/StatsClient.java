package ru.practicum.statsclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statsdto.HitCreateRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    public StatsClient(RestTemplateBuilder restTemplateBuilder,
                       @Value("${services.stats-service.uri}") String statsServiceUri) {
        super(restTemplateBuilder
                .uriTemplateHandler(new DefaultUriBuilderFactory(statsServiceUri))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build());
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Map<String, Object> params = Map.of("start", start, "end", end, "uris", uris, "unique", unique);

        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", params);
    }

    public ResponseEntity<Object> addHit(HitCreateRequest hitCreateRequest) {
        return post("/hit", hitCreateRequest);
    }
}
