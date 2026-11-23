import { MemberRole } from "../enums/associations/membership-role.type";


export interface Membership {
    id: number;
    userId: number;
    associationId: number;
    role: MemberRole;
    joinedAt: string;
}