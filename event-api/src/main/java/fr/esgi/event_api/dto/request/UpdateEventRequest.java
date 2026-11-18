package fr.esgi.event_api.dto.request;

import java.util.Date;

import fr.esgi.event_api.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class UpdateEventRequest {

    @Size(min = 2, max = 50, message = "Le titre de l'événement doit contenir entre 2 et 50 caractères.")
    private String title;

    @Size(max = 300, message = "La description de l'association ne peut pas dépasser 300 caractères.")
    private String description;

    private Date date;

    @NotBlank(message = "Le lieu de l'événement ne peut pas être vide.")
    private String location;

    @Positive(message = "La capacité doit être supérieure à zéro.")
    private Integer capacity;

    private EventStatus status;

}
