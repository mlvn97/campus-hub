package fr.esgi.user_api.dto.request;

import fr.esgi.user_api.enums.StudentGender;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Pattern;
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

    private StudentGender studentGender;

    @Size(max = 350_000)
    @Pattern(regexp = "^(?:data:image/jpeg;base64,[A-Za-z0-9+/]+={0,2})?$")
    private String profilePictureData;
}
