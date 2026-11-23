package fr.esgi.association_api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.esgi.association_api.dto.request.SubscriptionRequest;
import fr.esgi.association_api.dto.response.SubscriptionResponse;
import fr.esgi.association_api.entities.Membership;
import fr.esgi.association_api.entities.Subscription;
import fr.esgi.association_api.enums.MemberRole;
import fr.esgi.association_api.enums.SubscriptionStatus;
import fr.esgi.association_api.exceptions.AssociationNotFoundException;
import fr.esgi.association_api.exceptions.DuplicateRequestException;
import fr.esgi.association_api.exceptions.ForbiddenException;
import fr.esgi.association_api.exceptions.MembershipAlreadyExistsException;
import fr.esgi.association_api.exceptions.SubscriptionAlreadyManaged;
import fr.esgi.association_api.exceptions.SubscriptionNotFound;
import fr.esgi.association_api.repositories.AssociationRepository;
import fr.esgi.association_api.repositories.SubscriptionRepository;
import fr.esgi.association_api.repositories.MembershipRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final AssociationRepository associationRepository;
    private final SubscriptionRepository applicationRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipService membershipService;

    public SubscriptionResponse approveSubscription(Long userId, Long associationId, Long subscriptionId) {
        if (!membershipService.isPresidentOf(associationId, userId))
            throw new ForbiddenException("Seul le président de l'association peut gérer les demandes d'adhésion.");

        Subscription subscription = find(subscriptionId);
        checkPendingStatus(subscription);
        subscription.setStatus(SubscriptionStatus.APPROVED);
        membershipService.createMembershipFor(associationId, userId);
        return toResponse(subscription);
    }

    public SubscriptionResponse rejectSubscription(Long userId, Long associationId, Long subscriptionId) {
        if (!membershipService.isPresidentOf(associationId, userId))
            throw new ForbiddenException("Seul le président de l'association peut gérer les demandes d'adhésion.");

        Subscription subscription = find(subscriptionId);
        checkPendingStatus(subscription);
        subscription.setStatus(SubscriptionStatus.REJECTED);
        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse submitApplication(
            Long associationId,
            Long applicantUserId,
            SubscriptionRequest request) {
        checkIfAssociationExists(associationId);
        if (applicationRepository.existsByAssociationIdAndUserIdAndStatus(
                associationId, applicantUserId, SubscriptionStatus.PENDING)) {
            throw new DuplicateRequestException(applicantUserId, associationId);
        }
        if (membershipRepository.existsByAssociationIdAndUserId(associationId, applicantUserId)) {
            throw new MembershipAlreadyExistsException(associationId, applicantUserId);
        }

        Subscription application = new Subscription(
                null,
                applicantUserId,
                associationId,
                request.getMotivation(),
                SubscriptionStatus.PENDING
        );
        return toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptions(Long associationId, Long requesterUserId) {
        requirePresident(associationId, requesterUserId);
        return applicationRepository.findByAssociationIdOrderByIdDesc(associationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SubscriptionResponse approveApplication(
            Long associationId,
            Long applicationId,
            Long requesterUserId) {
        Subscription application = findPendingApplication(associationId, applicationId, requesterUserId);
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

        application.setStatus(SubscriptionStatus.APPROVED);
        return toResponse(applicationRepository.save(application));
    }

    @Transactional
    public SubscriptionResponse rejectApplication(
            Long associationId,
            Long applicationId,
            Long requesterUserId) {
        Subscription application = findPendingApplication(associationId, applicationId, requesterUserId);
        application.setStatus(SubscriptionStatus.REJECTED);
        return toResponse(applicationRepository.save(application));
    }

    private Subscription findPendingApplication(
            Long associationId,
            Long applicationId,
            Long requesterUserId) {
        requirePresident(associationId, requesterUserId);
        Subscription application = applicationRepository.findByIdAndAssociationId(applicationId, associationId)
                .orElseThrow(() -> new SubscriptionNotFound());
        if (application.getStatus() != SubscriptionStatus.PENDING) {
            throw new SubscriptionAlreadyManaged(applicationId);
        }
        return application;
    }

    private Subscription find(Long subscriptionId) {
        Subscription subscription = applicationRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFound());
        return subscription;
    }

    private void checkPendingStatus(Subscription subscription) {
        if (subscription.getStatus() != SubscriptionStatus.PENDING)
            throw new SubscriptionAlreadyManaged(subscription.getAssociationId());
    }

    private void requirePresident(Long associationId, Long requesterUserId) {
        checkIfAssociationExists(associationId);
        if (!membershipRepository.existsByAssociationIdAndUserIdAndRole(
                associationId, requesterUserId, MemberRole.PRESIDENT)) {
            throw new ForbiddenException("Seul le président de l'association peut gérer les demandes d'adhésion.");
        }
    }

    private void checkIfAssociationExists(Long associationId) {
        if (!associationRepository.existsById(associationId)) {
            throw new AssociationNotFoundException(associationId);
        }
    }

    private SubscriptionResponse toResponse(Subscription application) {
        return new SubscriptionResponse(
                application.getId(),
                application.getUserId(),
                application.getAssociationId(),
                application.getMotivation(),
                application.getStatus()
        );
    }
}
