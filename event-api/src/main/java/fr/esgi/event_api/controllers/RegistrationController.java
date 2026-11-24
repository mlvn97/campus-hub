package fr.esgi.event_api.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import fr.esgi.event_api.dto.response.RegistrationResponse;
import fr.esgi.event_api.services.RegistrationService;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/events/{eventId}/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping("/me")
    public ResponseEntity<RegistrationResponse> getMyRegistration(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId) {
        var registration = registrationService.getMyRegistration(userId, eventId);
        return registration.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
    
    @PostMapping 
    public ResponseEntity<RegistrationResponse> registerToEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        RegistrationResponse response = registrationService.registerToEvent(userId, eventId, authorization);
        return ResponseEntity.created(URI.create("/api/events/" + eventId + "/registrations/" + response.getId())).body(response);
    }

    @PatchMapping ("/{registrationId}")
    public ResponseEntity<RegistrationResponse> cancelRegistration(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId,
            @PathVariable Long registrationId) {
        return ResponseEntity.ok(registrationService.cancelRegistration(userId, eventId, registrationId));
    }
}
