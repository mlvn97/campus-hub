package fr.esgi.association_api.dto.response;

import fr.esgi.association_api.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private Long userId;
    private Long associationId;
    private String motivation;
    private SubscriptionStatus status;
}
