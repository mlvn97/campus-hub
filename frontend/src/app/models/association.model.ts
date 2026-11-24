export type AssociationCategory = 'SPORT' | 'CULTURE' | 'TECH' | 'HUMANITARIAN' | 'OTHER';
export type AssociationStatus = 'ACTIVE' | 'SUSPENDED';

export interface Association {
    id: number;
    name: string;
    description: string | null;
    category: AssociationCategory;
    status: AssociationStatus;
}


