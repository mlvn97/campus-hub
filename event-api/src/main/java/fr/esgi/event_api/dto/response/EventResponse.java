package fr.esgi.event_api.dto.response;

import java.util.Date;

import fr.esgi.event_api.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor 
public class EventResponse {

    private Long id;
    private Long associationId;
    private String title;
    private String description;
    private Date date;
    private String location;
    private Integer capacity;
    private EventStatus status;
}
