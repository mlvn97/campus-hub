package fr.esgi.association_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esgi.association_api.entities.Membership;
import fr.esgi.association_api.enums.MemberRole;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByAssociationIdOrderByJoinedAtAsc(Long associationId);

    Optional<Membership> findByAssociationIdAndUserId(Long associationId, Long userId);

    boolean existsByAssociationIdAndUserId(Long associationId, Long userId);

    boolean existsByAssociationIdAndRole(Long associationId, MemberRole role);

    boolean existsByUserIdAndRole(Long userId, MemberRole role);

    boolean existsByAssociationIdAndUserIdAndRole(
            Long associationId,
            Long userId,
            MemberRole role);

    void deleteAllByAssociationId(Long associationId);
}
