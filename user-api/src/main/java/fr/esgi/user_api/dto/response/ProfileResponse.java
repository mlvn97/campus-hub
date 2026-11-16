package fr.esgi.user_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class ProfileResponse {
    
    private Long userId;
    private String bio;
    private String profilePictureUrl;
    private String createdAt;

}
