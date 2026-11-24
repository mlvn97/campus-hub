package fr.esgi.association_api.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fr.esgi.association_api.dto.request.AddMemberRequest;
import fr.esgi.association_api.dto.response.MembershipResponse;
import fr.esgi.association_api.entities.Membership;
import fr.esgi.association_api.enums.MemberRole;
import fr.esgi.association_api.exceptions.AlreadyPresidentException;
import fr.esgi.association_api.exceptions.InvalidSuccessorException;
import fr.esgi.association_api.exceptions.MembershipNotFoundException;
import fr.esgi.association_api.exceptions.AlreadyHasPresidentException;
import fr.esgi.association_api.exceptions.PresidentRequiredException;
import fr.esgi.association_api.repositories.MembershipRepository;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final AssociationService associationService;
    private final MembershipRepository membershipRepository;

    public List<MembershipResponse> getMembers(Long associationId) {
        associationService.assertAssociationExists(associationId);
        return membershipRepository.findByAssociationIdOrderByJoinedAtAsc(associationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<MembershipResponse> getMyMembership(Long associationId, Long userId) {
        associationService.assertAssociationExists(associationId);
        return membershipRepository.findByAssociationIdAndUserId(associationId, userId)
                .map(this::toResponse);
    }

    public void leave(Long associationId, Long userId, @Nullable Long successorId) {
        Membership membership = findMember(associationId, userId);
        if (membership.getRole() == MemberRole.PRESIDENT) {
            if (successorId == null) throw new PresidentRequiredException();
            if (successorId.equals(userId)) throw new InvalidSuccessorException();

            Membership successor = findMember(associationId, successorId);
            if (membershipRepository.existsByUserIdAndRole(successorId, MemberRole.PRESIDENT))
                throw new AlreadyPresidentException(successorId);

            successor.setRole(MemberRole.PRESIDENT);
            membershipRepository.save(successor);
        }
        membershipRepository.delete(membership);
    }

    public void removeMember(Long associationId, Long userId) {
        Membership membership = findMember(associationId, userId);
        membershipRepository.delete(membership);
    }

    public MembershipResponse addMember(Long associationId, AddMemberRequest request) {
        if (request.getRole() == MemberRole.PRESIDENT) {
            if (membershipRepository.existsByUserIdAndRole(request.getUserId(), MemberRole.PRESIDENT))
                throw new AlreadyPresidentException(request.getUserId());
            if (membershipRepository.existsByAssociationIdAndRole(associationId, MemberRole.PRESIDENT))
                throw new AlreadyHasPresidentException(associationId);
        }
        Membership membership = new Membership(null, request.getUserId(), associationId, request.getRole(), LocalDateTime.now());
        return toResponse(membership);
    }

    public MembershipResponse changeMemberRole(Long associationId, Long userId, @NotNull MemberRole role ) {
        Membership membership = findMember(associationId, userId);

        if (membership.getRole() == MemberRole.PRESIDENT && role != MemberRole.PRESIDENT)
            throw new PresidentRequiredException();

        if (membership.getRole() != MemberRole.PRESIDENT && role == MemberRole.PRESIDENT) {
            if (membershipRepository.existsByUserIdAndRole(userId, MemberRole.PRESIDENT))
                throw new AlreadyPresidentException(userId);
            if (membershipRepository.existsByAssociationIdAndRole(associationId, MemberRole.PRESIDENT))
                throw new AlreadyHasPresidentException(associationId);
        }
        membership.setRole(role);
        return toResponse(membershipRepository.save(membership));
    }

    public boolean isPresidentOf(Long associationId, Long userId) {
        return membershipRepository.existsByAssociationIdAndUserIdAndRole(associationId, userId, MemberRole.PRESIDENT);
    }

    protected void createMembershipFor(Long associationId, Long userId) {
        Membership membership = new Membership(null, userId, associationId, MemberRole.MEMBER, LocalDateTime.now());
        membershipRepository.save(membership);
    }

    private Membership findMember(Long associationId, Long userId) {
        associationService.assertAssociationExists(associationId);
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
