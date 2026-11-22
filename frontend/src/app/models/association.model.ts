export type AssociationCategory = 'SPORT' | 'CULTURE' | 'TECH' | 'HUMANITARIAN' | 'OTHER';
export type AssociationStatus = 'ACTIVE' | 'SUSPENDED';
export type MemberRole = 'PRESIDENT' | 'TREASURER' | 'MEMBER';
export type MembershipApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Association {
    id: number;
    name: string;
    description: string | null;
    category: AssociationCategory;
    status: AssociationStatus;
}

export interface Membership {
    id: number;
    userId: number;
    associationId: number;
    role: MemberRole;
    joinedAt: string;
}

export interface MembershipApplication {
    id: number;
    userId: number;
    associationId: number;
    motivation: string | null;
    status: MembershipApplicationStatus;
}
