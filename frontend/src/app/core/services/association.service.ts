import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { AssociationRequest } from '../../dto/association/association-request.dto';
import { PageQuery, PageResponse } from '../../dto/page-response.dto';
import { Association, AssociationCategory, AssociationStatus } from '../../models/association.model';
import { ApiService } from './api.service';
import { queryParams } from './query-params';
import { Subscription } from '../../models/subscription.model';
import { SubscriptionRequest } from '../../dto/association/subscription/membership-subscription-request.dto';

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

    fetchApplications(associationId: number): Observable<Subscription[]> {
        return this.http.get<Subscription[]>(`${this.url}/${associationId}/membership-applications`);
    }

    submitApplication(associationId: number, data: SubscriptionRequest): Observable<Subscription> {
        return this.http.post<Subscription>(`${this.url}/${associationId}/membership-applications`, data);
    }

    acceptApplication(associationId: number, applicationId: number): Observable<Subscription> {
        return this.http.patch<Subscription>(
            `${this.url}/${associationId}/membership-applications/${applicationId}/accept`, null
        );
    }

    rejectApplication(associationId: number, applicationId: number): Observable<Subscription> {
        return this.http.patch<Subscription>(
            `${this.url}/${associationId}/membership-applications/${applicationId}/reject`, null
        );
    }
}
