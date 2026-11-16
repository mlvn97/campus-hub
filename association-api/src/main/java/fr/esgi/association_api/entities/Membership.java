package fr.esgi.association_api.entities;

import java.time.LocalDateTime;

import fr.esgi.association_api.enums.MemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "memberships", uniqueConstraints = @UniqueConstraint(
        name = "uk_membership_association_user",
        columnNames = {"association_id", "user_id"}))
@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class Membership {

    @Id 
    @GeneratedValue (strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "association_id", nullable = false)
    private Long associationId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
}
