package fr.esgi.association_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SubscriptionAlreadyManaged extends RuntimeException {

    public SubscriptionAlreadyManaged(Long applicationId) {
        super("Membership application " + applicationId + " has already been processed.");
    }
}
