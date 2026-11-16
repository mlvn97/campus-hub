package fr.esgi.association_api.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esgi.association_api.entities.Association;
import fr.esgi.association_api.enums.AssociationCategory;
import fr.esgi.association_api.enums.AssociationStatus;

@Repository 
public interface AssociationRepository extends JpaRepository<Association, Long> {

    Page<Association> findByCategory(AssociationCategory category, Pageable pageable);

    Page<Association> findByStatus(AssociationStatus status, Pageable pageable);

    Page<Association> findByCategoryAndStatus(
            AssociationCategory category,
            AssociationStatus status,
            Pageable pageable);
}
