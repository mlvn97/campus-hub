package fr.esgi.association_api.controllers;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.esgi.association_api.dto.request.AssociationRequest;
import fr.esgi.association_api.dto.response.AssociationResponse;
import fr.esgi.association_api.enums.AssociationCategory;
import fr.esgi.association_api.enums.AssociationStatus;
import fr.esgi.association_api.services.AssociationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/associations")
public class AssociationController {

    private final AssociationService associationService;

    @GetMapping
    public ResponseEntity<Page<AssociationResponse>> getAssociations(
            @RequestParam(required = false) AssociationCategory category,
            @RequestParam(required = false) AssociationStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(associationService.getAssociations(category, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssociationResponse> getAssociation(@PathVariable Long id) {
        return ResponseEntity.ok(associationService.getAssociation(id));
    }

    @PreAuthorize ("hasRole('ROLE_ADMIN_ASSOCIATION')")
    @PostMapping
    public ResponseEntity<AssociationResponse> createAssociation(
            @Valid @RequestBody AssociationRequest request) {
        AssociationResponse association = associationService.createAssociation(request);
        return ResponseEntity.created(URI.create("/api/associations/" + association.getId()))
                .body(association);
    }

    @PreAuthorize ("hasRole('ROLE_ADMIN_ASSOCIATION')")
    @PatchMapping("/{id}")
    public ResponseEntity<AssociationResponse> updateAssociation(
            @PathVariable Long id,
            @Valid @RequestBody AssociationRequest request) {
        return ResponseEntity.ok(associationService.updateAssociation(id, request));
    }

    @PreAuthorize ("hasRole('ROLE_ADMIN_ASSOCIATION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssociation(@PathVariable Long id) {
        associationService.deleteAssociation(id);
        return ResponseEntity.noContent().build();
    }
}
