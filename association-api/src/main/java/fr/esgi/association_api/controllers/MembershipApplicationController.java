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

import fr.esgi.association_api.dto.request.MembershipApplicationRequest;
import fr.esgi.association_api.dto.response.MembershipApplicationResponse;
import fr.esgi.association_api.services.MembershipApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/associations/{associationId}/membership-applications")
public class MembershipApplicationController {

    private final MembershipApplicationService applicationService;

    @PostMapping
    public ResponseEntity<MembershipApplicationResponse> submitApplication(
            @PathVariable Long associationId,
            @AuthenticationPrincipal Long applicantUserId,
            @Valid @RequestBody MembershipApplicationRequest request) {
        MembershipApplicationResponse application =
                applicationService.submitApplication(associationId, applicantUserId, request);
        return ResponseEntity.created(URI.create(
                "/api/associations/" + associationId + "/membership-applications/" + application.getId()))
                .body(application);
    }

    @GetMapping
    public ResponseEntity<List<MembershipApplicationResponse>> getApplications(
            @PathVariable Long associationId,
            @AuthenticationPrincipal Long requesterUserId) {
        return ResponseEntity.ok(applicationService.getApplications(associationId, requesterUserId));
    }

    @PatchMapping("/{applicationId}/accept")
    public ResponseEntity<MembershipApplicationResponse> acceptApplication(
            @PathVariable Long associationId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal Long requesterUserId) {
        return ResponseEntity.ok(
                applicationService.approveApplication(associationId, applicationId, requesterUserId));
    }

    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<MembershipApplicationResponse> rejectApplication(
            @PathVariable Long associationId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal Long requesterUserId) {
        return ResponseEntity.ok(
                applicationService.rejectApplication(associationId, applicationId, requesterUserId));
    }
}
