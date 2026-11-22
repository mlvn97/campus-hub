package fr.esgi.event_api.exceptions;

public class EventNotAvailableException extends RuntimeException {
    
    public EventNotAvailableException() {
        super("Événement indisponible.");
    }
}
