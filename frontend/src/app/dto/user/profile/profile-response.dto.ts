export type StudentGender = 'NON_RENSEIGNE' | 'MASCULIN' | 'FEMININ';

export interface ProfileResponse {
    userId: number,
    bio: string | null,
    studentGender: StudentGender | null,
    profilePictureData: string | null,
    profilePictureUrl: string | null,
    createdAt: string
}