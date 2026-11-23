import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { Subscription } from "../../models/subscription.model";
import { Observable } from "rxjs";
import { SubscriptionRequest } from "../../dto/association/subscription/membership-subscription-request.dto";

@Injectable({
    providedIn: 'root'
})
export class SubscriptionService {

    constructor(private http: HttpClient, private api: ApiService) {}

    private getUrl(associationId: number): string {
        return `${this.api.associationurl}/${associationId}/subscriptions`;
    }

    fetchSubscriptionsOf(associationId: number): Observable<Subscription[]> {
        return this.http.get<Subscription[]>(this.getUrl(associationId));
    }

    submitSubscriptionFor(associationId: number, data: SubscriptionRequest): Observable<Subscription> {
        return this.http.post<Subscription>(this.getUrl(associationId), data);
    }

    approveSubscription(associationId: number, subscriptionId: number): Observable<Subscription> {
        return this.http.patch<Subscription>(`${this.getUrl(associationId)}/${subscriptionId}/approve`, null);
    }

    rejectSubscription(associationid: number, subscriptionId: number): Observable<Subscription> {
        return this.http.patch<Subscription>(`${this.getUrl(associationid)}/${subscriptionId}/reject`, null);
    }
}