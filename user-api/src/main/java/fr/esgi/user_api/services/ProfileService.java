package fr.esgi.user_api.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import fr.esgi.user_api.dto.request.ProfileUpdateRequest;
import fr.esgi.user_api.dto.response.ProfileResponse;
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
            null,
            null,
            LocalDateTime.now()
        ));
    }

    public ProfileResponse getProfileByUserId(Long userId) {
        Profile profile = find(userId);
        return toProfileResponse(profile);
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        Profile profile = find(userId);
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getProfilePictureUrl() != null) {
            profile.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        if (request.getStudentGender() != null) {
            profile.setStudentGender(request.getStudentGender());
        }
        if (request.getProfilePictureData() != null) {
            profile.setProfilePictureData(request.getProfilePictureData().isEmpty() ? null : request.getProfilePictureData());
        }
        return toProfileResponse(profileRepository.save(profile));
    }

    private Profile find(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFound("Profil introuvable"));
    }

    private ProfileResponse toProfileResponse(Profile profile) {
        return new ProfileResponse(
            profile.getUserId(),
            profile.getBio(),
            profile.getProfilePictureUrl(),
            profile.getStudentGender(),
            profile.getProfilePictureData(),
            profile.getCreatedAt().toString()
        );
    }

}
