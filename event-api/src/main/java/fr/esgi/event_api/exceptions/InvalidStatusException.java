package fr.esgi.event_api.exceptions;

public class InvalidStatusException extends RuntimeException {
    
    public InvalidStatusException() {
        super("Status invalide.");
    }
}
