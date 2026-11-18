package fr.esgi.event_api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import fr.esgi.event_api.exceptions.ForbiddenException;

@Component
public class AssociationAuthorizationClient {

    private final RestClient restClient;

    public AssociationAuthorizationClient(
            RestClient.Builder restClientBuilder,
            @Value("${association-api.url}") String associationApiUrl) {
        this.restClient = restClientBuilder.baseUrl(associationApiUrl).build();
    }

    public void requireEventManagementAccess(Long associationId, String authorization) {
        try {
            // Transmet le JWT d'origine pour que l'API associations contrôle le même utilisateur.
            restClient.get()
                    .uri("/api/associations/{id}/event-management-access", associationId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Forbidden exception) {
            throw new ForbiddenException("Seul le président de l'association peut modifier ses événements.");
        }
    }
}
