package fr.esgi.event_api.dto.response;

import java.time.LocalDateTime;

import fr.esgi.event_api.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class RegistrationResponse {

    private Long id;
    private Long userId;
    private Long eventId;
    private LocalDateTime registrationDate;
    private RegistrationStatus status;
}
