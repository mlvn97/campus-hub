package fr.esgi.association_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esgi.association_api.entities.Subscription;
import fr.esgi.association_api.enums.SubscriptionStatus;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByAssociationIdOrderByIdDesc(Long associationId);

    Optional<Subscription> findByIdAndAssociationId(Long id, Long associationId);

    boolean existsByAssociationIdAndUserIdAndStatus(
            Long associationId,
            Long userId,
            SubscriptionStatus status);

    void deleteAllByAssociationId(Long associationId);
}
