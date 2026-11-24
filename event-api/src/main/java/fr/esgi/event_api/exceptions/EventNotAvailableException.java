package fr.esgi.event_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EventNotAvailableException extends RuntimeException {
    
    public EventNotAvailableException() {
        super("Événement indisponible.");
    }
}
