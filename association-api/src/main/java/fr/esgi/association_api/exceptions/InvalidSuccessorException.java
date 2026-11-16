package fr.esgi.association_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSuccessorException extends RuntimeException {

    public InvalidSuccessorException() {
        super("Le successeur doit être un autre membre de l'association.");
    }
}
