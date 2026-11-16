package fr.esgi.association_api.entities;

import fr.esgi.association_api.enums.AssociationCategory;
import fr.esgi.association_api.enums.AssociationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "associations")
@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class Association {
    
    @Id 
    @GeneratedValue (strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @NotBlank (message = "Le nom de l'association est obligatoire.")
    @Size (min = 2, max = 50, message = "Le nom de l'association doit contenir entre 2 et 50 caractères.")
    @Column (nullable = false)
    private String name;

    @Size (max = 300, message = "La description de l'association ne peut pas dépasser 300 caractères.")
    private String description;

    @NotNull (message = "La catégorie de l'association est obligatoire.")
    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private AssociationCategory category;

    @NotNull (message = "Le statut de l'association est obligatoire.")
    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private AssociationStatus status;

}
