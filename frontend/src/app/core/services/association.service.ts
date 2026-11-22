import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
    AddMemberRequest,
    AssociationRequest,
    ChangeMemberRoleRequest,
    MembershipApplicationRequest
} from '../../dto/association/association-request.dto';
import { PageQuery, PageResponse } from '../../dto/page-response.dto';
import {
    Association,
    AssociationCategory,
    AssociationStatus,
    Membership,
    MembershipApplication
} from '../../models/association.model';
import { ApiService } from './api.service';
import { queryParams } from './query-params';

export interface AssociationFilters extends PageQuery {
    category?: AssociationCategory;
    status?: AssociationStatus;
}

@Injectable({
    providedIn: 'root'
})
export class AssociationService {
    private readonly url: string;

    constructor(private http: HttpClient, api: ApiService) {
        this.url = api.associationurl;
    }

    fetchAssociations(filters: AssociationFilters = {}): Observable<PageResponse<Association>> {
        return this.http.get<PageResponse<Association>>(this.url, {
            params: queryParams({ ...filters })
        });
    }

    fetchAssociation(id: number): Observable<Association> {
        return this.http.get<Association>(`${this.url}/${id}`);
    }

    createAssociation(data: AssociationRequest): Observable<Association> {
        return this.http.post<Association>(this.url, data);
    }

    updateAssociation(id: number, data: AssociationRequest): Observable<Association> {
        return this.http.put<Association>(`${this.url}/${id}`, data);
    }

    deleteAssociation(id: number): Observable<void> {
        return this.http.delete<void>(`${this.url}/${id}`);
    }

    fetchMembers(associationId: number): Observable<Membership[]> {
        return this.http.get<Membership[]>(`${this.url}/${associationId}/members`);
    }

    addMember(associationId: number, data: AddMemberRequest): Observable<Membership> {
        return this.http.post<Membership>(`${this.url}/${associationId}/members`, data);
    }

    removeMember(associationId: number, userId: number, successorUserId?: number): Observable<void> {
        return this.http.delete<void>(`${this.url}/${associationId}/members/${userId}`, {
            params: queryParams({ successorUserId })
        });
    }

    changeMemberRole(associationId: number, userId: number, data: ChangeMemberRoleRequest): Observable<Membership> {
        return this.http.patch<Membership>(`${this.url}/${associationId}/members/${userId}/role`, data);
    }

    canManageAssociation(associationId: number): Observable<boolean> {
        return this.http.get<boolean>(`${this.url}/${associationId}/members/can-manage`);
    }

    fetchApplications(associationId: number): Observable<MembershipApplication[]> {
        return this.http.get<MembershipApplication[]>(`${this.url}/${associationId}/membership-applications`);
    }

    submitApplication(associationId: number, data: MembershipApplicationRequest): Observable<MembershipApplication> {
        return this.http.post<MembershipApplication>(`${this.url}/${associationId}/membership-applications`, data);
    }

    acceptApplication(associationId: number, applicationId: number): Observable<MembershipApplication> {
        return this.http.patch<MembershipApplication>(
            `${this.url}/${associationId}/membership-applications/${applicationId}/accept`, null
        );
    }

    rejectApplication(associationId: number, applicationId: number): Observable<MembershipApplication> {
        return this.http.patch<MembershipApplication>(
            `${this.url}/${associationId}/membership-applications/${applicationId}/reject`, null
        );
    }
}
