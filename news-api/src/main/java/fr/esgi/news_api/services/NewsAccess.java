package fr.esgi.news_api.services;

import fr.esgi.news_api.client.AssociationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class NewsAccess {
    private final AssociationClient associations;

    boolean canManage(Authentication actor, Long associationId, String authorization) {
        if (isCampusAdmin(actor)) {
            return true;
        }
        return associationId != null && associations.isPresident(associationId, authorization);
    }

    boolean isCampusAdmin(Authentication actor) {
        return actor.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN_CAMPUS"));
    }

    boolean associationExists(long associationId, String authorization) {
        return associations.associationExists(associationId, authorization);
    }
}
