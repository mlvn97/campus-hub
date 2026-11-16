package fr.esgi.association_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRequestException extends RuntimeException {

    public DuplicateRequestException(Long userId, Long associationId) {
        super("User " + userId + " has already applied to association " + associationId);
    }
}
