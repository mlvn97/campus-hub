package fr.esgi.association_api.dto.response;

import fr.esgi.association_api.enums.MembershipApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MembershipApplicationResponse {

    private Long id;
    private Long userId;
    private Long associationId;
    private String motivation;
    private MembershipApplicationStatus status;
}
