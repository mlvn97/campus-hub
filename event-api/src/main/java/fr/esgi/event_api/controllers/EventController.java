package fr.esgi.event_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;

import java.net.URI;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.esgi.event_api.dto.request.EventRequest;
import fr.esgi.event_api.dto.request.UpdateEventRequest;
import fr.esgi.event_api.dto.response.EventResponse;
import fr.esgi.event_api.enums.EventStatus;
import fr.esgi.event_api.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Événements")
@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/events")
public class EventController {
    
    private final EventService eventService;

    @GetMapping ("/{id}")
    @Operation(summary = "Consulter un événement")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un événement")
    @ApiResponse(responseCode = "201", description = "Ressource créée", useReturnTypeSchema = true)
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        EventResponse response = eventService.create(request, authorization);
        return ResponseEntity.created(URI.create("/api/events/" + response.getId())).body(response);
    }

    @PatchMapping ("/{id}")
    @Operation(summary = "Modifier un événement")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(eventService.update(request, id, authorization));
    }

    @PatchMapping ("/{id}/state")
    @Operation(summary = "Changer le statut d’un événement")
    public ResponseEntity<EventResponse> updateEventState(
            @PathVariable Long id,
            @RequestParam("state") String state,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(eventService.updateEventState(id, state, authorization));
    }

    @GetMapping 
    @Operation(summary = "Lister et filtrer les événements")
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) Long associationId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime date,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(eventService.filterEvents(status, associationId, date, pageable));
    }

}
