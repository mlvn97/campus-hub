package fr.esgi.user_api.services;

import org.springframework.stereotype.Service;

import fr.esgi.user_api.dto.request.UserRegistrationRequest;
import fr.esgi.user_api.dto.response.UserResponse;
import fr.esgi.user_api.exceptions.EmailAlreadyUsedException;
import fr.esgi.user_api.models.User;
import fr.esgi.user_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new EmailAlreadyUsedException("L'email est déjà utilisé.");

        User user = toEntity(request);
        return toUserResponse(userRepository.save(user));
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
            request.getPassword(),
            request.getRole()
        );
    }
}
