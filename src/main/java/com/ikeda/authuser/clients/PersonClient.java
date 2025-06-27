package com.ikeda.authuser.clients;

import com.ikeda.authuser.dtos.PersonRecordDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class PersonClient {

    Logger logger = LogManager.getLogger(PersonClient.class);

    @Value("${ikeda.api.url.operational}")
    String baseUrlPerson;

    final RestClient restClient;

    public PersonClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public PersonRecordDto getPersonUser(UUID userId){
        String url = baseUrlPerson + "/persons/" + userId + "/user";
        logger.debug("Request URL: {} ", url);

        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<PersonRecordDto>() {

                    });
        } catch (RestClientException e) {
            logger.error("Error Request RestClient with cause: {} ", e.getMessage());
            throw new RuntimeException("Error Request RestClient", e);
        }
    }
}
