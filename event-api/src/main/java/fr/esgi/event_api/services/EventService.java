package fr.esgi.event_api.services;

import org.springframework.stereotype.Service;

import fr.esgi.event_api.dto.request.EventRequest;
import fr.esgi.event_api.dto.request.UpdateEventRequest;
import fr.esgi.event_api.dto.response.EventResponse;
import fr.esgi.event_api.entities.Event;
import fr.esgi.event_api.exceptions.ForbiddenException;
import fr.esgi.event_api.exceptions.ResourceNotFound;
import fr.esgi.event_api.repositories.EventRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class EventService {
    
    private final EventRepository eventRepository;
    private final AssociationAuthorizationClient associationAuthorizationClient;

    public EventResponse getById(Long id) {
        return toResponse(find(id));
    }

    public EventResponse create(EventRequest eventRequest) {
        Event event = toEntity(eventRequest);
        return toResponse(eventRepository.save(event));
    }

    public EventResponse update(UpdateEventRequest request, Long eventId, String authorization) {
        Event event = find(eventId);
        // Utilise l'association enregistrée, et non un identifiant fourni dans le PATCH.
        associationAuthorizationClient.requireEventManagementAccess(event.getAssociationId(), authorization);
        
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getDate() != null) event.setDate(request.getDate());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getCapacity() != null) event.setCapacity(request.getCapacity());
        if (request.getStatus() != null) event.setStatus(request.getStatus());
        return toResponse(eventRepository.save(event));
    }

    public void delete(EventRequest request, Long eventId, Long associationId) {
        Event event = find(eventId);
        if (request.getAssociationId() != associationId)
            throw new ForbiddenException("Une association peut seulement supprimer ses propres événements.");
        
        eventRepository.delete(event);
    }

    private Event find(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Événement " + id + " introuvable."));
    }

    private Event toEntity(EventRequest eventRequest) {
        return new Event(
            null,
            eventRequest.getAssociationId(),
            eventRequest.getTitle(),
            eventRequest.getDescription(),
            eventRequest.getDate(),
            eventRequest.getLocation(),
            eventRequest.getCapacity(),
            eventRequest.getStatus()
        );
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
            event.getId(),
            event.getAssociationId(),
            event.getTitle(),
            event.getDescription(),
            event.getDate(),
            event.getLocation(),
            event.getCapacity(),
            event.getStatus()
        );
    }
}
