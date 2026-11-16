package fr.esgi.association_api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.esgi.association_api.dto.request.MembershipApplicationRequest;
import fr.esgi.association_api.dto.response.MembershipApplicationResponse;
import fr.esgi.association_api.entities.Membership;
import fr.esgi.association_api.entities.MembershipApplication;
import fr.esgi.association_api.enums.MemberRole;
import fr.esgi.association_api.enums.MembershipApplicationStatus;
import fr.esgi.association_api.exceptions.AssociationNotFoundException;
import fr.esgi.association_api.exceptions.DuplicateRequestException;
import fr.esgi.association_api.exceptions.ForbiddenException;
import fr.esgi.association_api.exceptions.MembershipAlreadyExistsException;
import fr.esgi.association_api.exceptions.MembershipApplicationAlreadyProcessedException;
import fr.esgi.association_api.exceptions.MembershipApplicationNotFoundException;
import fr.esgi.association_api.repositories.AssociationRepository;
import fr.esgi.association_api.repositories.MembershipApplicationRepository;
import fr.esgi.association_api.repositories.MembershipRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipApplicationService {

    private final AssociationRepository associationRepository;
    private final MembershipApplicationRepository applicationRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public MembershipApplicationResponse submitApplication(
            Long associationId,
            Long applicantUserId,
            MembershipApplicationRequest request) {
        requireAssociation(associationId);
        if (applicationRepository.existsByAssociationIdAndUserIdAndStatus(
                associationId, applicantUserId, MembershipApplicationStatus.PENDING)) {
            throw new DuplicateRequestException(applicantUserId, associationId);
        }
        if (membershipRepository.existsByAssociationIdAndUserId(associationId, applicantUserId)) {
            throw new MembershipAlreadyExistsException(associationId, applicantUserId);
        }

        MembershipApplication application = new MembershipApplication(
                null,
                applicantUserId,
                associationId,
                request.getMotivation(),
                MembershipApplicationStatus.PENDING
        );
        return toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<MembershipApplicationResponse> getApplications(Long associationId, Long requesterUserId) {
        requirePresident(associationId, requesterUserId);
        return applicationRepository.findByAssociationIdOrderByIdDesc(associationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MembershipApplicationResponse approveApplication(
            Long associationId,
            Long applicationId,
            Long requesterUserId) {
        MembershipApplication application = findPendingApplication(associationId, applicationId, requesterUserId);
        if (membershipRepository.existsByAssociationIdAndUserId(associationId, application.getUserId())) {
            throw new MembershipAlreadyExistsException(associationId, application.getUserId());
        }

        Membership membership = new Membership(
                null,
                application.getUserId(),
                associationId,
                MemberRole.MEMBER,
                LocalDateTime.now()
        );
        membershipRepository.save(membership);

        application.setStatus(MembershipApplicationStatus.APPROVED);
        return toResponse(applicationRepository.save(application));
    }

    @Transactional
    public MembershipApplicationResponse rejectApplication(
            Long associationId,
            Long applicationId,
            Long requesterUserId) {
        MembershipApplication application = findPendingApplication(associationId, applicationId, requesterUserId);
        application.setStatus(MembershipApplicationStatus.REJECTED);
        return toResponse(applicationRepository.save(application));
    }

    private MembershipApplication findPendingApplication(
            Long associationId,
            Long applicationId,
            Long requesterUserId) {
        requirePresident(associationId, requesterUserId);
        MembershipApplication application = applicationRepository.findByIdAndAssociationId(applicationId, associationId)
                .orElseThrow(() -> new MembershipApplicationNotFoundException(applicationId, associationId));
        if (application.getStatus() != MembershipApplicationStatus.PENDING) {
            throw new MembershipApplicationAlreadyProcessedException(applicationId);
        }
        return application;
    }

    private void requirePresident(Long associationId, Long requesterUserId) {
        requireAssociation(associationId);
        if (!membershipRepository.existsByAssociationIdAndUserIdAndRole(
                associationId, requesterUserId, MemberRole.PRESIDENT)) {
            throw new ForbiddenException("Seul le président de l'association peut gérer les demandes d'adhésion.");
        }
    }

    private void requireAssociation(Long associationId) {
        if (!associationRepository.existsById(associationId)) {
            throw new AssociationNotFoundException(associationId);
        }
    }

    private MembershipApplicationResponse toResponse(MembershipApplication application) {
        return new MembershipApplicationResponse(
                application.getId(),
                application.getUserId(),
                application.getAssociationId(),
                application.getMotivation(),
                application.getStatus()
        );
    }
}
