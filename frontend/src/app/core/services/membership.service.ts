import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { Observable } from "rxjs";
import { Membership } from "../../models/membership.model";
import { AddMemberRequest } from "../../dto/association/add-member-request.dto";
import { ChangeMemberRoleRequest } from "../../dto/association/change-member-role-request.dto";
import { queryParams } from "./query-params";

@Injectable({
    providedIn: 'root'
})
export class MembershipService {

    private associationId: number = 1;

    private url: string;

    constructor(private http: HttpClient, private api: ApiService) {
        this.url = `${this.api.associationurl}/${this.associationId}/members`;
    }

    isPresident(associationId: number): Observable<boolean> {
        this.associationId = associationId;
        return this.http.get<boolean>(`${this.url}/can-manage`);
    }

    fetchMembers(associationId: number): Observable<Membership[]> {
        this.associationId = associationId;
        return this.http.get<Membership[]>(this.url);
    }

    addMember(associationId: number, data: AddMemberRequest): Observable<Membership> {
        this.associationId = associationId;
        return this.http.post<Membership>(this.url, data);
    }

    changeMemberRole(associationId: number, userId: number, data: ChangeMemberRoleRequest): Observable<Membership> {
        this.associationId = associationId;
        return this.http.patch<Membership>(`${this.url}/${userId}/role`, data);
    }

    removeMember(associationId: number, userId: number, successorUserId: number): Observable<void> {
        this.associationId = associationId;
        return this.http.delete<void>(`${this.url}/members/${userId}`, {
            params: queryParams({ successorUserId })
        });
    }

}