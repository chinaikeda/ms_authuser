package com.ikeda.authuser.clients;

import com.ikeda.authuser.dtos.ResponsePageDto;
import com.ikeda.authuser.dtos.UserRecordDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OperationalClient {
//    TODO - AI - Somente teste

    Logger logger = LogManager.getLogger(OperationalClient.class);

    @Value("${ikeda.api.url.operational}")
    String baseUrlOperational;

    final RestClient restClient;

    public OperationalClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

//    @Retry(name = "retryInstance", fallbackMethod = "retryfallback")
    @CircuitBreaker(name = "circuitbreakerInstance", fallbackMethod = "circuitbreakerfallback")
    public Page<UserRecordDto> getOperationalUser(UUID userId, Pageable pageable, String token){
        String url = baseUrlOperational + "/users?userId=" + userId + "&page=" + pageable.getPageNumber() + "&size="
                + pageable.getPageSize() + "&sort=" + pageable.getSort().toString().replaceAll(": ", ",");
        logger.debug("Request URL: {} ", url);

        try {
            return restClient.get()
                    .uri(url)
                    .header("Authorization", token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ResponsePageDto<UserRecordDto>>() {});
        } catch (HttpStatusCodeException e){
            logger.error("Error Request RestClient with status: {}, cause: {}", e.getStatusCode(), e.getMessage());
            switch (e.getStatusCode()){
                case HttpStatus.FORBIDDEN -> throw new AccessDeniedException("Forbidden");
                default -> throw new RuntimeException("Error Request RestClient", e);
            }
        } catch (RestClientException e){
            logger.error("Error Request RestClient with cause: {} ", e.getMessage());
            throw new RuntimeException("Error Request RestClient", e);
        }
    }

    public Page<UserRecordDto> retryfallback(UUID userId, Pageable pageable, Throwable t){
        logger.debug("Inside retry retryfallback, cause - {}", t.toString());
        List<UserRecordDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }

    public Page<UserRecordDto> circuitbreakerfallback(UUID userId, Pageable pageable, Throwable t){
        logger.debug("Inside circuit breaker fallback, cause - {}", t.toString());
        List<UserRecordDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }
}
