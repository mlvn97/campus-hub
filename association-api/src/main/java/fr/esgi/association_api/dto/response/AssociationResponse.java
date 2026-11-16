package fr.esgi.association_api.dto.response;

import fr.esgi.association_api.enums.AssociationCategory;
import fr.esgi.association_api.enums.AssociationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssociationResponse {

    private Long id;
    private String name;
    private String description;
    private AssociationCategory category;
    private AssociationStatus status;
}
