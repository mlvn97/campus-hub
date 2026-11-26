package fr.esgi.association_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;

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

@Tag(name = "Associations")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/associations")
public class AssociationController {

    private final AssociationService associationService;

    @GetMapping
    @Operation(summary = "Lister et filtrer les associations")
    public ResponseEntity<Page<AssociationResponse>> getAssociations(
            @RequestParam(required = false) AssociationCategory category,
            @RequestParam(required = false) AssociationStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(associationService.getAssociations(category, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une association")
    public ResponseEntity<AssociationResponse> getAssociation(@PathVariable Long id) {
        return ResponseEntity.ok(associationService.getAssociation(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN_ASSOCIATION', 'ADMIN_CAMPUS')")
    @PostMapping
    @Operation(summary = "Créer une association")
    @ApiResponse(responseCode = "201", description = "Ressource créée", useReturnTypeSchema = true)
    public ResponseEntity<AssociationResponse> createAssociation(
            @Valid @RequestBody AssociationRequest request) {
        AssociationResponse association = associationService.createAssociation(request);
        return ResponseEntity.created(URI.create("/api/associations/" + association.getId()))
                .body(association);
    }

    @PreAuthorize ("hasRole('ADMIN_ASSOCIATION')")
    @PatchMapping("/{id}")
    @Operation(summary = "Modifier une association")
    public ResponseEntity<AssociationResponse> updateAssociation(
            @PathVariable Long id,
            @Valid @RequestBody AssociationRequest request) {
        return ResponseEntity.ok(associationService.updateAssociation(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN_CAMPUS', 'ADMIN_ASSOCIATION')")
    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspendre une association")
    public ResponseEntity<AssociationResponse> suspendAssociation(@PathVariable Long id) {
        return ResponseEntity.ok(associationService.suspendAssociation(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN_CAMPUS', 'ADMIN_ASSOCIATION')")
    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Réactiver une association")
    public ResponseEntity<AssociationResponse> reactivateAssociation(@PathVariable Long id) {
        return ResponseEntity.ok(associationService.reactivateAssociation(id));
    }

    @PreAuthorize ("hasRole('ADMIN_ASSOCIATION')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une association")
    @ApiResponse(responseCode = "204", description = "Succès sans contenu", content = @io.swagger.v3.oas.annotations.media.Content)
    public ResponseEntity<Void> deleteAssociation(@PathVariable Long id) {
        associationService.deleteAssociation(id);
        return ResponseEntity.noContent().build();
    }
}
