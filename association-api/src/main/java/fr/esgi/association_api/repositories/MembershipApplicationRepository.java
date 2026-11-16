package fr.esgi.association_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esgi.association_api.entities.MembershipApplication;
import fr.esgi.association_api.enums.MembershipApplicationStatus;

@Repository
public interface MembershipApplicationRepository extends JpaRepository<MembershipApplication, Long> {

    List<MembershipApplication> findByAssociationIdOrderByIdDesc(Long associationId);

    Optional<MembershipApplication> findByIdAndAssociationId(Long id, Long associationId);

    boolean existsByAssociationIdAndUserIdAndStatus(
            Long associationId,
            Long userId,
            MembershipApplicationStatus status);

    void deleteAllByAssociationId(Long associationId);
}
