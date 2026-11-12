package fr.esgi.user_api.models;

import fr.esgi.user_api.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Désigne la table <code>users</code>. <br>
 * Contient:
 * <ul>
 *   <li>id: l'identifiant unique de l'utilisateur</li>
 *   <li>name: son nom</li>
 *   <li>email: son email</li>
 *   <li>password: son mot de passe</li>
 *   <li>role: son rôle (voir {@link UserRole})</li>
 * </ul>
 */
@Entity
@Table (name = "users")
@AllArgsConstructor 
@NoArgsConstructor 
@Data 
public class User {
    
    @Id
    @GeneratedValue (strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @NotBlank (message = "Le nom est obligatoire.")
    @Size (min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères.")
    @Column (nullable = false)
    private String name;

    @NotBlank (message = "L'email ne peut pas être vide.")
    @Email (message = "Format d'email invalide.")
    @Column (nullable = false)
    private String email;

    @NotBlank (message = "Le mot de passe ne peut pas être vide")
    @Column (nullable = false)
    private String password;

    @NotNull (message = "Le rôle est obligatoire.")
    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private UserRole role;

}
