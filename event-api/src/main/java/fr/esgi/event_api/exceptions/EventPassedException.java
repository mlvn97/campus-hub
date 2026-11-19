package fr.esgi.event_api.exceptions;

public class EventPassedException extends RuntimeException {
    
    public EventPassedException() {
        super("Événement déjà passé.");
    }
}
