package fr.esgi.association_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyHasPresidentException extends RuntimeException {

    public AlreadyHasPresidentException(Long associationId) {
        super("Association " + associationId + " already has a president.");
    }
}
