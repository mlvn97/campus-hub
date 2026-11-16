package fr.esgi.association_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MembershipAlreadyExistsException extends RuntimeException {

    public MembershipAlreadyExistsException(Long associationId, Long userId) {
        super("User " + userId + " is already a member of association " + associationId);
    }
}
