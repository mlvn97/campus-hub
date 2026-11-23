export type MemberRole = 'PRESIDENT' | 'TREASURER' | 'MEMBER';

export interface Membership {
    id: number;
    userId: number;
    associationId: number;
    role: MemberRole;
    joinedAt: string;
}