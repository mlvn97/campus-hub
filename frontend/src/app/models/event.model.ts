export type EventStatus = 'DRAFT' | 'POSTED' | 'COMPLETE' | 'OVER' | 'CANCELLED';
export type RegistrationStatus = 'CONFIRMED' | 'PENDING' | 'CANCELLED';

export interface Event {
    id: number;
    associationId: number;
    title: string;
    description: string | null;
    date: string;
    location: string;
    capacity: number;
    status: EventStatus;
}

export interface Registration {
    id: number;
    userId: number;
    eventId: number;
    registrationDate: string;
    status: RegistrationStatus;
}
