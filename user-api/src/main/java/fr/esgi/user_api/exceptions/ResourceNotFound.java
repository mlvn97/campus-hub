package fr.esgi.user_api.exceptions;

public class ResourceNotFound extends RuntimeException {
    
    public ResourceNotFound(String message) {
        super(message);
    }
    
}
