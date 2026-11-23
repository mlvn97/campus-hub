package fr.esgi.association_api.controllers;

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
import fr.esgi.association_api.enums.SubscriptionStatus;
import fr.esgi.association_api.services.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/associations/{associationId}/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> submitApplication(
            @PathVariable Long associationId,
            @AuthenticationPrincipal Long applicantUserId,
            @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse subscription =
                subscriptionService.submitApplication(associationId, applicantUserId, request);
        return ResponseEntity.created(URI.create(
                "/api/associations/" + associationId + "/subscriptions/" + subscription.getId()))
                .body(subscription);
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(
            @PathVariable Long associationId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptions(associationId, userId));
    }

    @PatchMapping("/{subscriptionId}/approve")
    public ResponseEntity<SubscriptionResponse> approveSubscription(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long associationId,
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionService.approveSubscription(userId, associationId, subscriptionId));
    }
}
