package fr.esgi.user_api.dto.request;

import fr.esgi.user_api.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class UserRegistrationRequest {
    
    @NotBlank (message = "Le nom est obligatoire.")
    @Size (min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères.")
    private String name;

    @NotBlank (message = "L'email est obligatoire.")
    @Email (message = "Format d'email invalide.")
    private String email;

    @NotBlank (message = "Le mot de passe est obligatoire.")
    @Size (min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères.")
    private String password;

    @NotNull (message = "Le rôle est obligatoire.")
    private UserRole role;

}
