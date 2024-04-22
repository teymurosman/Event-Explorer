package ru.practicum.mainservice.statsclient;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {

    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path, @Nullable Map<String, Object> params) {
        return makeAndSendRequest(HttpMethod.GET, path, params, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, null, body);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path,
                                                          @Nullable Map<String, Object> params, @Nullable T body) {
        HttpEntity<T> httpEntity = new HttpEntity<>(body, getDefaultHeaders());

        ResponseEntity<Object> responseEntity;
        try {
            if (params == null) {
                responseEntity = rest.exchange(path, method, httpEntity, Object.class);
            } else {
                responseEntity = rest.exchange(path, method, httpEntity, Object.class, params);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }

        return getResponse(responseEntity);
    }

    private HttpHeaders getDefaultHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }

    private ResponseEntity<Object> getResponse(ResponseEntity<Object> responseEntity) {
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity;
        }

        ResponseEntity.BodyBuilder responseBodyBuilder = ResponseEntity.status(responseEntity.getStatusCode());

        if (responseEntity.hasBody()) {
            return responseBodyBuilder.body(responseEntity.getBody());
        }

        return responseBodyBuilder.build();
    }
}
