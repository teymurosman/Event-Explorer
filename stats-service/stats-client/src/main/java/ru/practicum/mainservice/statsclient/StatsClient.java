package ru.practicum.mainservice.statsclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statsdto.HitCreateRequest;
import ru.practicum.statsdto.StatsResponse;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplateBuilder restTemplateBuilder,
                       @Value("${services.stats-service.url}") String statsServiceUrl) {
        super(restTemplateBuilder
                .uriTemplateHandler(new DefaultUriBuilderFactory(statsServiceUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build());
    }

    public List<StatsResponse> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startString = start.format(FORMATTER);
        String endString = end.format(FORMATTER);
        Map<String, Object> params = Map.of("start", startString, "end", endString, "uris", uris, "unique", unique);

        ResponseEntity<Object> response =
                get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", params);
        return objectMapper.convertValue(response.getBody(), new TypeReference<ArrayList<StatsResponse>>() {});
    }

    public ResponseEntity<Object> addHit(HttpServletRequest httpRequest) {
        HitCreateRequest hitCreateRequest = HitCreateRequest.builder()
                .app("ewm-main-service")
                .uri(httpRequest.getRequestURI())
                .ip(httpRequest.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
        return post("/hit", hitCreateRequest);
    }
}
