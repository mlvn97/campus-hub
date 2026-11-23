import { MemberRole } from "../../models/membership.model";

export interface AddMemberRequest {
    userId: number;
    role: MemberRole;
}