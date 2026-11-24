package fr.esgi.event_api.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fr.esgi.event_api.client.AssociationClient;
import fr.esgi.event_api.dto.response.RegistrationResponse;
import fr.esgi.event_api.entities.Event;
import fr.esgi.event_api.entities.Registration;
import fr.esgi.event_api.enums.EventStatus;
import fr.esgi.event_api.enums.RegistrationStatus;
import fr.esgi.event_api.exceptions.EventPassedException;
import fr.esgi.event_api.exceptions.ForbiddenException;
import fr.esgi.event_api.exceptions.ResourceNotFound;
import fr.esgi.event_api.exceptions.AlreadyRegisteredException;
import fr.esgi.event_api.exceptions.EventNotAvailableException;
import fr.esgi.event_api.repositories.RegistrationRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class RegistrationService {
    
    private final EventService eventService;
    private final RegistrationRepository registrationRepository;
    private final AssociationClient associationClient;

    public RegistrationResponse registerToEvent(Long userId, Long eventId, String authorization) {
        Event event = eventService.find(eventId);
        if (associationClient.canManageEvent(event.getAssociationId(), authorization)) {
            throw new ForbiddenException("Le président de l'association ne peut pas s'inscrire à son événement.");
        }

        EventStatus status = event.getStatus();
        if (status == EventStatus.CANCELLED || status == EventStatus.DRAFT)
            throw new EventNotAvailableException();

        LocalDateTime eventDate = event.getDate();
        if (eventDate.isBefore(LocalDateTime.now()))
            throw new EventPassedException();

        if (registrationRepository.existsByUserIdAndEventIdAndStatus(userId, eventId, RegistrationStatus.CONFIRMED) ||
            registrationRepository.existsByUserIdAndEventIdAndStatus(userId, eventId, RegistrationStatus.PENDING))
            throw new AlreadyRegisteredException();
        
        RegistrationStatus registrationStatus = RegistrationStatus.CONFIRMED;
        if (eventIsComplete(event)) {
            registrationStatus = RegistrationStatus.PENDING;
            eventService.changeEventStatus(eventId, EventStatus.COMPLETE);
        }

        Registration registration = new Registration(null, userId, eventId, LocalDateTime.now(), registrationStatus);
        return toResponse(registrationRepository.save(registration));
    }

    public RegistrationResponse cancelRegistration(Long userId, Long eventId, Long registrationId) {
        Registration registration = find(registrationId);
        if (!registration.getUserId().equals(userId) || !registration.getEventId().equals(eventId)) {
            throw new ForbiddenException("Vous ne pouvez annuler que votre propre inscription.");
        }

        RegistrationStatus oldStatus = registration.getStatus();
        registration.setStatus(RegistrationStatus.CANCELLED);
        Registration cancelled = registrationRepository.save(registration);

        if (oldStatus == RegistrationStatus.CONFIRMED) {
            Optional<Registration> optFirstPending =
                registrationRepository.findFirstByEventIdAndStatusOrderByRegistrationDateAsc(eventId, RegistrationStatus.PENDING);

            if (optFirstPending.isPresent()) {
                Registration firstPending = optFirstPending.get();
                firstPending.setStatus(RegistrationStatus.CONFIRMED);
                registrationRepository.save(firstPending);
            } else {
                eventService.changeEventStatus(eventId, EventStatus.POSTED);
            }
        }

        return toResponse(cancelled);
    }

    public Optional<RegistrationResponse> getMyRegistration(Long userId, Long eventId) {
        eventService.find(eventId);
        List<RegistrationStatus> activeStatuses = List.of(
                RegistrationStatus.CONFIRMED,
                RegistrationStatus.PENDING);
        return registrationRepository
                .findFirstByUserIdAndEventIdAndStatusIn(userId, eventId, activeStatuses)
                .map(this::toResponse);
    }

    public List<RegistrationResponse> retrieveRegistrationsOf(Long eventId) {
        List<RegistrationStatus> statuses = List.of(RegistrationStatus.CONFIRMED, RegistrationStatus.PENDING);
        List<Registration> registrations = registrationRepository.findByEventIdAndStatusIn(eventId, statuses);
        return registrations.stream().map(this::toResponse).toList();
    }

    /**
     * Vérifie si un événement est complet.
     * @param e - L'événement
     * @return Vrai si l'événement est complet, Faux sinon
     */
    private boolean eventIsComplete(Event e) {
        RegistrationStatus registrationStatus = RegistrationStatus.CONFIRMED;
        Integer registrations = registrationRepository.countByEventIdAndStatus(e.getId(), registrationStatus);
        return registrations == e.getCapacity();
    }

    private Registration find(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Inscription n°" + id + " introuvable"));
    }

    private RegistrationResponse toResponse(Registration inscription) {
        return new RegistrationResponse(
            inscription.getId(),
            inscription.getUserId(),
            inscription.getEventId(),
            inscription.getRegistrationDate(),
            inscription.getStatus()
        );
    }
}
