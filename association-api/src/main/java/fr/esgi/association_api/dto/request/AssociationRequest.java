package fr.esgi.association_api.dto.request;

import fr.esgi.association_api.enums.AssociationCategory;
import fr.esgi.association_api.enums.AssociationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssociationRequest {

    @NotBlank(message = "Le nom de l'association est obligatoire.")
    @Size(min = 2, max = 50, message = "Le nom de l'association doit contenir entre 2 et 50 caractères.")
    private String name;

    @Size(max = 300, message = "La description de l'association ne peut pas dépasser 300 caractères.")
    private String description;

    @NotNull(message = "La catégorie de l'association est obligatoire.")
    private AssociationCategory category;

    @NotNull(message = "Le statut de l'association est obligatoire.")
    private AssociationStatus status;
}
