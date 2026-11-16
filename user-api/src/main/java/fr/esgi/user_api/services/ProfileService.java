package fr.esgi.user_api.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import fr.esgi.user_api.dto.request.ProfileUpdateRequest;
import fr.esgi.user_api.dto.response.ProfileResponse;
import fr.esgi.user_api.exceptions.ForbiddenException;
import fr.esgi.user_api.exceptions.ResourceNotFound;
import fr.esgi.user_api.models.Profile;
import fr.esgi.user_api.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    private static final String DEFAULT_PROFILE_PICTURE_URL = "https://www.google.com/url?sa=t&source=web&rct=j&url=https%3A%2F%2Fpixabay.com%2Ffr%2Fimages%2Fsearch%2Fphoto%2520de%2520profil%2F&ved=0CBcQjRxqFwoTCOiA-7XVjJgDFQAAAAAdAAAAABA4&opi=89978449";

    public void create(Long userId) {
        profileRepository.save(new Profile(
            null,
            userId,
            null,
            DEFAULT_PROFILE_PICTURE_URL,
            LocalDateTime.now()
        ));
    }

    public ProfileResponse getProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFound("Profile not found"));
        return toProfileResponse(profile);
    }

    public ProfileResponse updateProfile(Long profileOwnerId, Long requesterId, ProfileUpdateRequest request) {
        if (!profileOwnerId.equals(requesterId)) {
            throw new ForbiddenException("Vous ne pouvez modifier que votre propre profil.");
        }

        Profile profile = profileRepository.findByUserId(profileOwnerId)
                .orElseThrow(() -> new ResourceNotFound("Profile not found"));
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getProfilePictureUrl() != null) {
            profile.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        return toProfileResponse(profileRepository.save(profile));
    }

    private ProfileResponse toProfileResponse(Profile profile) {
        return new ProfileResponse(
            profile.getUserId(),
            profile.getBio(),
            profile.getProfilePictureUrl(),
            profile.getCreatedAt().toString()
        );
    }

}
