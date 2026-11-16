package fr.esgi.association_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyPresidentException extends RuntimeException {

    public AlreadyPresidentException(Long userId) {
        super("User " + userId + " is already president of another association.");
    }
}
