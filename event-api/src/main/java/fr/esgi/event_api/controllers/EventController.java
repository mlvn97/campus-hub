package fr.esgi.event_api.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
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
import fr.esgi.event_api.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/events")
public class EventController {
    
    private final EventService eventService;

    @GetMapping ("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        EventResponse response = eventService.create(request, authorization);
        return ResponseEntity.created(URI.create("/api/events/" + response.getId())).body(response);
    }

    @PatchMapping ("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(eventService.update(request, id, authorization));
    }

    @PatchMapping ("/{id}/state")
    public ResponseEntity<EventResponse> updateEventState(
            @PathVariable Long id,
            @RequestParam("state") String state,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(eventService.updateEventState(id, state, authorization));
    }

}
