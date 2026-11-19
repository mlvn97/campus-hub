package fr.esgi.event_api.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component 
public class AssociationClient {
    
    private final RestClient restClient;

    public AssociationClient(RestClient.Builder builder, @Value ("${association-api.url}") String url) {
        this.restClient = builder.baseUrl(url).build();
    }

    public boolean canManageEvent(Long associationId, String authorization) {
        return restClient.get()
            .uri("/api/associations/{associationId}/members/can-manage", associationId)
            .header(authorization)
            .retrieve()
            .body(Boolean.class);
    }
}
