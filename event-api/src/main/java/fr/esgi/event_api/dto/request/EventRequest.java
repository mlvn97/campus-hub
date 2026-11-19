package fr.esgi.event_api.dto.request;

import java.time.LocalDateTime;

import fr.esgi.event_api.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {

    @NotNull(message = "L'identifiant de l'association est obligatoire.")
    private Long associationId;

    @NotBlank(message = "Le titre de l'événement est obligatoire.")
    @Size(min = 2, max = 50, message = "Le titre de l'événement doit contenir entre 2 et 50 caractères.")
    private String title;

    @Size(max = 300, message = "La description de l'association ne peut pas dépasser 300 caractères.")
    private String description;

    @NotNull(message = "La date de l'événement est obligatoire.")
    private LocalDateTime date;

    @NotBlank(message = "Le lieu de l'événement est obligatoire.")
    private String location;

    @NotNull(message = "La capacité de l'événement est obligatoire.")
    @Positive(message = "La capacité doit être supérieure à zéro.")
    private Integer capacity;

    @NotNull(message = "Le statut de l'événement est obligatoire.")
    private EventStatus status;
    
}
