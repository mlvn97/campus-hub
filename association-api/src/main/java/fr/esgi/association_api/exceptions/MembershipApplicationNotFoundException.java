package fr.esgi.association_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MembershipApplicationNotFoundException extends RuntimeException {

    public MembershipApplicationNotFoundException(Long applicationId, Long associationId) {
        super("Membership application " + applicationId + " not found for association " + associationId);
    }
}
