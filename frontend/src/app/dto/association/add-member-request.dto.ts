import { MemberRole } from "../../enums/associations/membership-role.type";

export interface AddMemberRequest {
    userId: number;
    role: MemberRole;
}