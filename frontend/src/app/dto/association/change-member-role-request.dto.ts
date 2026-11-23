import { MemberRole } from "../../models/membership.model";

export interface ChangeMemberRoleRequest {
    role: MemberRole;
}