package fr.esgi.association_api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.esgi.association_api.dto.request.AddMemberRequest;
import fr.esgi.association_api.dto.request.ChangeMemberRoleRequest;
import fr.esgi.association_api.dto.response.MembershipResponse;
import fr.esgi.association_api.entities.Membership;
import fr.esgi.association_api.enums.MemberRole;
import fr.esgi.association_api.exceptions.AlreadyPresidentException;
import fr.esgi.association_api.exceptions.AssociationNotFoundException;
import fr.esgi.association_api.exceptions.ForbiddenException;
import fr.esgi.association_api.exceptions.InvalidSuccessorException;
import fr.esgi.association_api.exceptions.MembershipAlreadyExistsException;
import fr.esgi.association_api.exceptions.MembershipNotFoundException;
import fr.esgi.association_api.exceptions.AlreadyHasPresidentException;
import fr.esgi.association_api.exceptions.PresidentRequiredException;
import fr.esgi.association_api.repositories.AssociationRepository;
import fr.esgi.association_api.repositories.MembershipRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final AssociationRepository associationRepository;
    private final MembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public List<MembershipResponse> getMembers(Long associationId) {
        requireAssociation(associationId);
        return membershipRepository.findByAssociationIdOrderByJoinedAtAsc(associationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MembershipResponse addMember(Long associationId, AddMemberRequest request) {
        requireAssociation(associationId);
        if (membershipRepository.existsByAssociationIdAndUserId(associationId, request.getUserId())) {
            throw new MembershipAlreadyExistsException(associationId, request.getUserId());
        }
        if (request.getRole() == MemberRole.PRESIDENT) {
            if (membershipRepository.existsByUserIdAndRole(request.getUserId(), MemberRole.PRESIDENT)) {
                throw new AlreadyPresidentException(request.getUserId());
            }
            if (membershipRepository.existsByAssociationIdAndRole(associationId, MemberRole.PRESIDENT)) {
                throw new AlreadyHasPresidentException(associationId);
            }
        }

        Membership membership = new Membership(
                null,
                request.getUserId(),
                associationId,
                request.getRole(),
                LocalDateTime.now()
        );
        return toResponse(membershipRepository.save(membership));
    }

    @Transactional
    public void removeMember(Long associationId, Long userId, Long successorUserId) {
        Membership membership = findMember(associationId, userId);
        if (membership.getRole() == MemberRole.PRESIDENT) {
            if (successorUserId == null) {
                throw new PresidentRequiredException();
            }
            if (successorUserId.equals(userId)) {
                throw new InvalidSuccessorException();
            }

            Membership successor = findMember(associationId, successorUserId);
            if (membershipRepository.existsByUserIdAndRole(successorUserId, MemberRole.PRESIDENT)) {
                throw new AlreadyPresidentException(successorUserId);
            }
            successor.setRole(MemberRole.PRESIDENT);
            membershipRepository.save(successor);
        }
        membershipRepository.delete(membership);
    }

    @Transactional
    public MembershipResponse changeMemberRole(
            Long associationId,
            Long userId,
            ChangeMemberRoleRequest request) {
        Membership membership = findMember(associationId, userId);
        if (membership.getRole() == MemberRole.PRESIDENT && request.getRole() != MemberRole.PRESIDENT) {
            throw new PresidentRequiredException();
        }
        if (membership.getRole() != MemberRole.PRESIDENT && request.getRole() == MemberRole.PRESIDENT) {
            if (membershipRepository.existsByUserIdAndRole(userId, MemberRole.PRESIDENT)) {
                throw new AlreadyPresidentException(userId);
            }
            if (membershipRepository.existsByAssociationIdAndRole(associationId, MemberRole.PRESIDENT)) {
                throw new AlreadyHasPresidentException(associationId);
            }
        }
        membership.setRole(request.getRole());
        return toResponse(membershipRepository.save(membership));
    }

    public boolean isPresidentOf(Long associationId, Long userId) {
        return membershipRepository.existsByAssociationIdAndUserIdAndRole(associationId, userId, MemberRole.PRESIDENT);
    }

    private void requireAssociation(Long associationId) {
        if (!associationRepository.existsById(associationId)) {
            throw new AssociationNotFoundException(associationId);
        }
    }

    private Membership findMember(Long associationId, Long userId) {
        requireAssociation(associationId);
        return membershipRepository.findByAssociationIdAndUserId(associationId, userId)
                .orElseThrow(() -> new MembershipNotFoundException(associationId, userId));
    }

    private MembershipResponse toResponse(Membership membership) {
        return new MembershipResponse(
                membership.getId(),
                membership.getUserId(),
                membership.getAssociationId(),
                membership.getRole(),
                membership.getJoinedAt()
        );
    }
}
