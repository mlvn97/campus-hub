package fr.esgi.association_api.dto.response;

import java.time.LocalDateTime;

import fr.esgi.association_api.enums.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MembershipResponse {

    private Long id;
    private Long userId;
    private Long associationId;
    private MemberRole role;
    private LocalDateTime joinedAt;
}
