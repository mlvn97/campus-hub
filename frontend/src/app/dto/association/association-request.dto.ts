import { Association, MemberRole } from '../../models/association.model';

export type AssociationRequest = Omit<Association, 'id' | 'description'> & {
    description?: string | null;
};

export interface AddMemberRequest {
    userId: number;
    role: MemberRole;
}

export interface ChangeMemberRoleRequest {
    role: MemberRole;
}

export interface MembershipApplicationRequest {
    motivation: string;
}
