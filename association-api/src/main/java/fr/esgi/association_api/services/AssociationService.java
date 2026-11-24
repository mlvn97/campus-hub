package fr.esgi.association_api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.esgi.association_api.dto.request.AssociationRequest;
import fr.esgi.association_api.dto.response.AssociationResponse;
import fr.esgi.association_api.entities.Association;
import fr.esgi.association_api.enums.AssociationCategory;
import fr.esgi.association_api.enums.AssociationStatus;
import fr.esgi.association_api.exceptions.AssociationNotFoundException;
import fr.esgi.association_api.repositories.AssociationRepository;
import fr.esgi.association_api.repositories.SubscriptionRepository;
import fr.esgi.association_api.repositories.MembershipRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssociationService {

    private final AssociationRepository associationRepository;
    private final MembershipRepository membershipRepository;
    private final SubscriptionRepository membershipApplicationRepository;

    @Transactional(readOnly = true)
    public Page<AssociationResponse> getAssociations(
            AssociationCategory category,
            AssociationStatus status,
            Pageable pageable) {
        Page<Association> associations;
        if (category != null && status != null) {
            associations = associationRepository.findByCategoryAndStatus(category, status, pageable);
        } else if (category != null) {
            associations = associationRepository.findByCategory(category, pageable);
        } else if (status != null) {
            associations = associationRepository.findByStatus(status, pageable);
        } else {
            associations = associationRepository.findAll(pageable);
        }
        return associations.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AssociationResponse getAssociation(Long id) {
        return toResponse(findAssociation(id));
    }

    @Transactional
    public AssociationResponse createAssociation(AssociationRequest request) {
        Association association = toEntity(request);
        return toResponse(associationRepository.save(association));
    }

    @Transactional
    public AssociationResponse updateAssociation(Long id, AssociationRequest request) {
        Association association = findAssociation(id);
        association.setName(request.getName());
        association.setDescription(request.getDescription());
        association.setCategory(request.getCategory());
        association.setStatus(request.getStatus());
        return toResponse(associationRepository.save(association));
    }

    @Transactional
    public AssociationResponse suspendAssociation(Long id) {
        Association association = findAssociation(id);
        association.setStatus(AssociationStatus.SUSPENDED);
        return toResponse(associationRepository.save(association));
    }

    @Transactional
    public AssociationResponse reactivateAssociation(Long id) {
        Association association = findAssociation(id);
        association.setStatus(AssociationStatus.ACTIVE);
        return toResponse(associationRepository.save(association));
    }

    @Transactional
    public void deleteAssociation(Long id) {
        Association association = findAssociation(id);
        membershipApplicationRepository.deleteAllByAssociationId(id);
        membershipRepository.deleteAllByAssociationId(id);
        associationRepository.delete(association);
    }

    protected void assertAssociationExists(Long id) {
        if (!associationRepository.existsById(id)) throw new AssociationNotFoundException(id);
    }

    private Association findAssociation(Long id) {
        return associationRepository.findById(id)
                .orElseThrow(() -> new AssociationNotFoundException(id));
    }

    private Association toEntity(AssociationRequest request) {
        return new Association(
                null,
                request.getName(),
                request.getDescription(),
                request.getCategory(),
                request.getStatus()
        );
    }

    private AssociationResponse toResponse(Association association) {
        return new AssociationResponse(
                association.getId(),
                association.getName(),
                association.getDescription(),
                association.getCategory(),
                association.getStatus()
        );
    }
}
