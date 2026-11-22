package fr.esgi.event_api.exceptions;

public class AlreadyRegisteredException extends RuntimeException {
    
    public AlreadyRegisteredException() {
        super("Utilisateur déjà inscrit à cet événement.");
    }
}
