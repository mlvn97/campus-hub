import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { Observable } from "rxjs";
import { Membership } from "../../models/membership.model";
import { AddMemberRequest } from "../../dto/association/add-member-request.dto";
import { ChangeMemberRoleRequest } from "../../dto/association/change-member-role-request.dto";
import { queryParams } from "./query-params";
import { MemberRole } from "../../enums/associations/membership-role.type";

@Injectable({
    providedIn: 'root'
})
export class MembershipService {

    constructor(private http: HttpClient, private api: ApiService) {}

    private getUrl(associationId: number): string {
        return `${this.api.associationurl}/${associationId}/members`;
    }

    isPresident(associationId: number): Observable<boolean> {
        return this.http.get<boolean>(`${this.getUrl(associationId)}/is-president`);
    }

    fetchMembers(associationId: number): Observable<Membership[]> {
        return this.http.get<Membership[]>(this.getUrl(associationId));
    }

    addMember(associationId: number, data: AddMemberRequest): Observable<Membership> {
        return this.http.post<Membership>(this.getUrl(associationId), data);
    }

    changeMemberRole(associationId: number, userId: number, role: MemberRole): Observable<Membership> {
        return this.http.patch<Membership>(`${this.getUrl(associationId)}/${userId}/role`, role);
    }

    removeMember(associationId: number, userId: number): Observable<void> {
        return this.http.delete<void>(`${this.getUrl(associationId)}/${userId}`);
    }

    leave(associationId: number, userId: number, successorUserId: number): Observable<void> {
        return this.http.delete<void>(`${this.getUrl(associationId)}/members/${userId}`, {
            params: queryParams({ successorUserId })
        });
    }

}