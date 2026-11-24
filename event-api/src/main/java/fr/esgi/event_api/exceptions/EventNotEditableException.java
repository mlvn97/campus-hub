package fr.esgi.event_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EventNotEditableException extends RuntimeException {

    public EventNotEditableException() {
        super("Seuls les événements brouillons peuvent être modifiés.");
    }
}
