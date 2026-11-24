package fr.esgi.user_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.esgi.user_api.dto.request.LoginRequest;
import fr.esgi.user_api.dto.request.UserRegistrationRequest;
import fr.esgi.user_api.dto.response.AuthenticationResponse;
import fr.esgi.user_api.dto.response.UserResponse;
import fr.esgi.user_api.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Comptes")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping ("/register")
    @Operation(summary = "Créer un compte")
    @SecurityRequirements
    @ApiResponse(responseCode = "201", description = "Ressource créée", useReturnTypeSchema = true)
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        URI location = URI.create("/api/users/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Obtenir un jeton JWT")
    @SecurityRequirements
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping ("/{id}")
    @Operation(summary = "Consulter un utilisateur")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
}
