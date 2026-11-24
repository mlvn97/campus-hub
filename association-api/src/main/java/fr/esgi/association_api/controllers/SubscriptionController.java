package fr.esgi.association_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.esgi.association_api.dto.request.SubscriptionRequest;
import fr.esgi.association_api.dto.response.SubscriptionResponse;
import fr.esgi.association_api.services.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Demandes d’adhésion")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/associations/{associationId}/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Déposer une demande")
    @ApiResponse(responseCode = "201", description = "Ressource créée", useReturnTypeSchema = true)
    public ResponseEntity<SubscriptionResponse> submitApplication(
            @PathVariable Long associationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long applicantUserId,
            @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse subscription =
                subscriptionService.submitApplication(associationId, applicantUserId, request);
        return ResponseEntity.created(URI.create(
                "/api/associations/" + associationId + "/subscriptions/" + subscription.getId()))
                .body(subscription);
    }

    @GetMapping
    @Operation(summary = "Lister les demandes")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(
            @PathVariable Long associationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptions(associationId, userId));
    }

    @PatchMapping("/{subscriptionId}/approve")
    @Operation(summary = "Accepter une demande")
    public ResponseEntity<SubscriptionResponse> approveSubscription(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long associationId,
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionService.approveApplication(associationId, subscriptionId, userId));
    }

    @PatchMapping("/{subscriptionId}/reject")
    @Operation(summary = "Refuser une demande")
    public ResponseEntity<SubscriptionResponse> rejectSubscription(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long associationId,
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionService.rejectApplication(associationId, subscriptionId, userId));
    }
}
