package fr.esgi.event_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

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

@Tag(name = "Inscriptions")
@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/events/{eventId}/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping("/me")
    @Operation(summary = "Consulter sa propre inscription")
    public ResponseEntity<RegistrationResponse> getMyRegistration(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId) {
        var registration = registrationService.getMyRegistration(userId, eventId);
        return registration.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
    
    @PostMapping 
    @Operation(summary = "S’inscrire à un événement")
    @ApiResponse(responseCode = "201", description = "Ressource créée", useReturnTypeSchema = true)
    public ResponseEntity<RegistrationResponse> registerToEvent(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        RegistrationResponse response = registrationService.registerToEvent(userId, eventId, authorization);
        return ResponseEntity.created(URI.create("/api/events/" + eventId + "/registrations/" + response.getId())).body(response);
    }

    @PatchMapping ("/{registrationId}")
    @Operation(summary = "Annuler une inscription")
    public ResponseEntity<RegistrationResponse> cancelRegistration(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId,
            @PathVariable Long registrationId) {
        return ResponseEntity.ok(registrationService.cancelRegistration(userId, eventId, registrationId));
    }
}
