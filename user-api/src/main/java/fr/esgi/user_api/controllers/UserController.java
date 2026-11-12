package fr.esgi.user_api.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import fr.esgi.user_api.dto.request.UserRegistrationRequest;
import fr.esgi.user_api.dto.response.UserResponse;
import fr.esgi.user_api.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Controller ("/api/users")
@AllArgsConstructor 
public class UserController {

    private final UserService userService;

    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        URI location = URI.create("/api/users/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }
    
}
