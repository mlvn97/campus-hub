package fr.esgi.association_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest {

    @NotBlank(message = "Le message de motivation est obligatoire.")
    @Size(max = 1000, message = "Le message de motivation ne peut pas dépasser 1000 caractères.")
    private String motivation;
}
