package fr.esgi.user_api.dto.request;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {

    @Size(max = 300, message = "La biographie ne peut pas dépasser 300 caractères.")
    private String bio;

    @URL(message = "L'URL de la photo de profil est invalide.")
    private String profilePictureUrl;
}
