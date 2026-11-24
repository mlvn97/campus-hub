package fr.esgi.event_api.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.esgi.event_api.client.AssociationClient;
import fr.esgi.event_api.dto.request.EventRequest;
import fr.esgi.event_api.dto.request.UpdateEventRequest;
import fr.esgi.event_api.dto.response.EventResponse;
import fr.esgi.event_api.entities.Event;
import fr.esgi.event_api.enums.EventStatus;
import fr.esgi.event_api.exceptions.ForbiddenException;
import fr.esgi.event_api.exceptions.EventNotEditableException;
import fr.esgi.event_api.exceptions.InvalidStatusException;
import fr.esgi.event_api.exceptions.ResourceNotFound;
import fr.esgi.event_api.repositories.EventRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class EventService {
    
    private final EventRepository eventRepository;
    private final AssociationClient associationClient;

    public EventResponse getById(Long id) {
        return toResponse(find(id));
    }

    public EventResponse create(EventRequest eventRequest, String authorization) {
        Event event = toEntity(eventRequest);
        assertCanManageEvent(event, authorization);
        event.setStatus(EventStatus.DRAFT);  // Un événement créé est mis en brouillon initialement
        return toResponse(eventRepository.save(event));
    }

    public EventResponse update(UpdateEventRequest request, Long eventId, String authorization) {
        Event event = find(eventId);
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventNotEditableException();
        }
        assertCanManageEvent(event, authorization);

        if (request.getAssociationId() != null
                && !request.getAssociationId().equals(event.getAssociationId())
                && !associationClient.canManageEvent(request.getAssociationId(), authorization)) {
            throw new ForbiddenException("Vous devez être président de l'association de destination.");
        }
        
        if (request.getAssociationId() != null) event.setAssociationId(request.getAssociationId());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getDate() != null) event.setDate(request.getDate());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getCapacity() != null) event.setCapacity(request.getCapacity());
        return toResponse(eventRepository.save(event));
    }

    public void delete(EventRequest request, Long eventId, Long associationId) {
        Event event = find(eventId);        
        eventRepository.delete(event);
    }

    public EventResponse updateEventState(Long eventId, String state, String authorization) {
        assertCanManageEvent(find(eventId), authorization);
        EventResponse response = changeEventStatus(eventId, normalizeStatus(state));
        return response;
    }

    public Page<EventResponse> filterEvents(EventStatus status, Long associationId, LocalDateTime date, Pageable pageable) {
        return eventRepository
                .findAll(EventSpecification.withFilters(status, associationId, date), pageable)
                .map(this::toResponse);
    }

    /**
     * Vérifie si l'utilisateur a le droit de modifier un événement.
     * @param e - L'événement à modifier
     * @param auth - L'utilisateur
     */
    private void assertCanManageEvent(Event e, String auth) {
        if (!associationClient.canManageEvent(e.getAssociationId(), auth))
            throw new ForbiddenException("Vous n'êtes pas le président de cette association");
    }

    private EventStatus normalizeStatus(String s) {
        for (EventStatus status : EventStatus.values()) {
            if (s.equals(status.name()))
                return status;
        }
        throw new InvalidStatusException();
    }

    protected EventResponse changeEventStatus(Long eventId, EventStatus status) {
        Event e = find(eventId);
        e.setStatus(status);
        return toResponse(eventRepository.save(e));
    }

    protected Event find(Long id) {
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
