package fr.esgi.user_api.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.URL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Désigne la table <code>profiles</code>. <br>
 * Contient:
 * <ul>
 *   <li>id: l'identifiant unique du profil</li>
 *   <li>userId: l'identifiant de l'utilisateur associé</li>
 *   <li>bio: la biographie de l'utilisateur</li>
 *   <li>profilePictureUrl: l'URL de la photo de profil</li>
 *   <li>created_at: la date de création du profil</li>
 * </ul>
 */
@Entity 
@Table (name = "profiles")
@AllArgsConstructor 
@NoArgsConstructor 
@Data 
public class Profile {

    @Id 
    @GeneratedValue (strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    
    @Size (max = 300)
    private String bio;

    @URL 
    private String profilePictureUrl;

    @CreationTimestamp 
    @Column (updatable = false)
    private LocalDateTime created_at;
    
}
