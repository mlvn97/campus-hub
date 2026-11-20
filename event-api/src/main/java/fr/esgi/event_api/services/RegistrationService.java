package fr.esgi.event_api.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fr.esgi.event_api.dto.response.RegistrationResponse;
import fr.esgi.event_api.entities.Event;
import fr.esgi.event_api.entities.Registration;
import fr.esgi.event_api.enums.EventStatus;
import fr.esgi.event_api.enums.RegistrationStatus;
import fr.esgi.event_api.exceptions.EventPassedException;
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

    public RegistrationResponse registerToEvent(Long userId, Long eventId) {
        Event event = eventService.find(eventId);

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
        RegistrationStatus oldStatus = registration.getStatus();
        registration.setStatus(RegistrationStatus.CANCELLED);

        // Si la personne qui se désinscrit était confirmée, alors le premier de la liste d'attente passe confirmé.
        if (oldStatus != RegistrationStatus.CONFIRMED)
            return toResponse(registrationRepository.save(registration));
        else {
            Optional<Registration> optFirstPending =
                registrationRepository.findFirstByEventIdAndStatusOrderByRegistrationDateAsc(eventId, RegistrationStatus.PENDING);

            // Le premier de la liste, s'il existe, devient confirmé et l'événement reste complet
            if (optFirstPending.isPresent()) {
                Registration firstPending = optFirstPending.get();
                firstPending.setStatus(RegistrationStatus.CONFIRMED);
                return toResponse(registrationRepository.save(firstPending));
            }

            // Sinon l'événement repasse en status POSTED
            eventService.changeEventStatus(eventId, EventStatus.POSTED);
            return toResponse(registrationRepository.save(registration));
        }
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

    // TODO: Filtrage des événements par status, association, date de début et de fin

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
