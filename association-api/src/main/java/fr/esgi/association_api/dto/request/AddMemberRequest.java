package fr.esgi.association_api.dto.request;

import fr.esgi.association_api.enums.MemberRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddMemberRequest {

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    private MemberRole role;
}
