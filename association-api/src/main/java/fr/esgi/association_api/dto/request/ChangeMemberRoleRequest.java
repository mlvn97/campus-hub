package fr.esgi.association_api.dto.request;

import fr.esgi.association_api.enums.MemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeMemberRoleRequest {

    @NotNull
    private MemberRole role;
}
