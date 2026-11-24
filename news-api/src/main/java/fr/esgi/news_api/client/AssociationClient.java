package fr.esgi.news_api.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AssociationClient {
    private final RestClient restClient;

    public AssociationClient(@Value("${association-api.url}") String url) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(requestFactory).build();
    }

    public boolean isPresident(long associationId, String authorization) {
        try {
            Boolean allowed = restClient.get()
                    .uri("/api/associations/{id}/members/is-president", associationId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(Boolean.class);
            return Boolean.TRUE.equals(allowed);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().equals(HttpStatus.FORBIDDEN)
                    || exception.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return false;
            }
            throw unavailable(exception);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    public boolean associationExists(long associationId, String authorization) {
        try {
            restClient.get()
                    .uri("/api/associations/{id}", associationId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().toBodilessEntity();
            return true;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return false;
            }
            throw unavailable(exception);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private static ResponseStatusException unavailable(RestClientException exception) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service des associations indisponible", exception);
    }
}
