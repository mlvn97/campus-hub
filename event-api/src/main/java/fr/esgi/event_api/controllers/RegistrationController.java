package fr.esgi.event_api.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.esgi.event_api.dto.response.RegistrationResponse;
import fr.esgi.event_api.services.RegistrationService;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/events/{id}/inscriptions")
public class RegistrationController {

    private final RegistrationService registrationService;
    
    @PostMapping 
    public ResponseEntity<RegistrationResponse> registerToEvent(@AuthenticationPrincipal Long userId, @PathVariable Long eventId) {
        RegistrationResponse response = registrationService.registerToEvent(userId, eventId);
        return ResponseEntity.created(URI.create("/api/events/" + eventId + "/inscriptions/" + response.getId())).body(response);
    }
}
