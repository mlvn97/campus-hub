package fr.esgi.association_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PresidentRequiredException extends RuntimeException {

    public PresidentRequiredException() {
        super("Un successeur doit être désigné avant le départ du président.");
    }
}
