package fr.esgi.association_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MembershipNotFoundException extends RuntimeException {

    public MembershipNotFoundException(Long associationId, Long userId) {
        super("User " + userId + " is not a member of association " + associationId);
    }
}
