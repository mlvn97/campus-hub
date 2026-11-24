import { StudentGender } from './profile-response.dto';

export interface ProfileUpdateRequest {
    bio?: string;
    studentGender?: StudentGender;
    profilePictureData?: string;
    profilePictureUrl?: string;
}
