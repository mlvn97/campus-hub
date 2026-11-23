import { Association } from '../../models/association.model';

export type AssociationRequest = Omit<Association, 'id' | 'description'> & {
    description?: string | null;
};

export interface MembershipApplicationRequest {
    motivation: string;
}
