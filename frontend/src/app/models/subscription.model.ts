import { SubscriptionStatus } from "../enums/associations/subscription.type";

export interface Subscription {
    id: number;
    userId: number;
    associationId: number;
    motivation: string | null;
    status: SubscriptionStatus;
}