package fr.esgi.association_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.esgi.association_api.dto.request.AddMemberRequest;
import fr.esgi.association_api.dto.request.ChangeMemberRoleRequest;
import fr.esgi.association_api.dto.response.MembershipResponse;
import fr.esgi.association_api.services.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Membres")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/associations/{associationId}/members")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping
    @Operation(summary = "Lister les membres")
    public ResponseEntity<List<MembershipResponse>> getMembers(
            @PathVariable Long associationId) {
        return ResponseEntity.ok(membershipService.getMembers(associationId));
    }

    @GetMapping("/me")
    @Operation(summary = "Consulter sa propre adhésion")
    public ResponseEntity<MembershipResponse> getMyMembership(
            @PathVariable Long associationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return membershipService.getMyMembership(associationId, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    @Operation(summary = "Ajouter un membre")
    @ApiResponse(responseCode = "201", description = "Ressource créée", content = @io.swagger.v3.oas.annotations.media.Content)
    public ResponseEntity<MembershipResponse> addMember(
            @PathVariable Long associationId,
            @Valid @RequestBody AddMemberRequest request) {
        MembershipResponse response = membershipService.addMember(associationId, request);
        return ResponseEntity.created(URI.create("/api/associations/" + associationId + "/members/" + response.getId())).build();
    }

    @DeleteMapping("/leave")
    @Operation(summary = "Quitter une association")
    @ApiResponse(responseCode = "204", description = "Succès sans contenu", content = @io.swagger.v3.oas.annotations.media.Content)
    public ResponseEntity<Void> leave(
            @PathVariable Long associationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long successorId) {
        membershipService.leave(associationId, userId, successorId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Retirer un membre")
    @ApiResponse(responseCode = "204", description = "Succès sans contenu", content = @io.swagger.v3.oas.annotations.media.Content)
    public ResponseEntity<Void> removeMember(
            @PathVariable Long associationId,
            @PathVariable Long userId) {
        membershipService.removeMember(associationId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "Modifier le rôle d’un membre")
    public ResponseEntity<MembershipResponse> changeMemberRole(
            @PathVariable Long associationId,
            @PathVariable Long userId,
            @Valid @RequestBody ChangeMemberRoleRequest request) {
        return ResponseEntity.ok(membershipService.changeMemberRole(associationId, userId, request.getRole()));
    }

    @GetMapping ("/is-president")
    @Operation(summary = "Vérifier si l’utilisateur préside l’association")
    public boolean isPresident(@PathVariable Long associationId, @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return membershipService.isPresidentOf(associationId, userId);
    }
}
