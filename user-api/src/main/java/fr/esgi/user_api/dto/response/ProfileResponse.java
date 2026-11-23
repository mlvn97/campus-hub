package fr.esgi.user_api.dto.response;

import fr.esgi.user_api.enums.StudentGender;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class ProfileResponse {
    
    private Long userId;
    private String bio;
    private String profilePictureUrl;

    private StudentGender studentGender;
    private String profilePictureData;
    private String createdAt;

}
