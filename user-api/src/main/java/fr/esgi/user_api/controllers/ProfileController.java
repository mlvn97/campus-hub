package fr.esgi.user_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.esgi.user_api.dto.request.ProfileUpdateRequest;
import fr.esgi.user_api.dto.response.ProfileResponse;
import fr.esgi.user_api.services.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Profils")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "Consulter son profil")
    public ResponseEntity<ProfileResponse> getMyProfile(@Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Consulter un profil")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @PatchMapping("/me")
    @Operation(summary = "Modifier son profil")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN_CAMPUS')")
    @PatchMapping("/{userId}")
    @Operation(summary = "Modifier un profil par identifiant")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }
}
