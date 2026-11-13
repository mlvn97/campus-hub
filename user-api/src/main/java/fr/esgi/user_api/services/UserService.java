package fr.esgi.user_api.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import fr.esgi.user_api.dto.request.LoginRequest;
import fr.esgi.user_api.dto.request.UserRegistrationRequest;
import fr.esgi.user_api.dto.response.AuthenticationResponse;
import fr.esgi.user_api.dto.response.UserResponse;
import fr.esgi.user_api.exceptions.EmailAlreadyUsedException;
import fr.esgi.user_api.exceptions.InvalidCredentialsException;
import fr.esgi.user_api.enums.UserRole;
import fr.esgi.user_api.models.User;
import fr.esgi.user_api.repositories.UserRepository;
import fr.esgi.user_api.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new EmailAlreadyUsedException("L'email est déjà utilisé.");

        User user = toEntity(request);
        return toAuthenticationResponse(userRepository.save(user));
    }

    public AuthenticationResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return toAuthenticationResponse(user);
    }

    private AuthenticationResponse toAuthenticationResponse(User user) {
        return new AuthenticationResponse(
                jwtService.generateToken(user.getId(), user.getRole().name()),
                "Bearer",
                toUserResponse(user)
        );
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }

    private User toEntity(UserRegistrationRequest request) {
        return new User(
            null, 
            request.getName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            UserRole.STUDENT
        );
    }
}
