package fr.esgi.event_api.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class RegistrationRequest {

    @NotNull (message = "La date d'inscription est obligatoire.")
    private LocalDateTime registrationDate;
}
