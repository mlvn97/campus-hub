import { Event } from '../../models/event.model';

export type EventRequest = Omit<Event, 'id' | 'description'> & {
    description?: string | null;
};

export type UpdateEventRequest = Pick<EventRequest, 'location'> &
    Partial<Omit<EventRequest, 'associationId' | 'status' | 'location'>>;
