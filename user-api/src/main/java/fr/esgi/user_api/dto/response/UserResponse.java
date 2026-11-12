package fr.esgi.user_api.dto.response;

import fr.esgi.user_api.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class UserResponse {
    
    private Long id;
    private String name;
    private String email;
    private UserRole role;

}
