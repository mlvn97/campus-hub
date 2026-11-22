import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { EventRequest, UpdateEventRequest } from '../../dto/event/event-request.dto';
import { PageQuery, PageResponse } from '../../dto/page-response.dto';
import { Event, EventStatus, Registration } from '../../models/event.model';
import { ApiService } from './api.service';
import { queryParams } from './query-params';

export interface EventFilters extends PageQuery {
    status?: EventStatus;
    associationId?: number;
    date?: string;
}

@Injectable({
    providedIn: 'root'
})
export class EventService {
    private readonly url: string;

    constructor(private http: HttpClient, api: ApiService) {
        this.url = api.eventUrl;
    }

    fetchEvents(filters: EventFilters = {}): Observable<PageResponse<Event>> {
        return this.http.get<PageResponse<Event>>(this.url, {
            params: queryParams({ ...filters })
        });
    }

    fetchEvent(id: number): Observable<Event> {
        return this.http.get<Event>(`${this.url}/${id}`);
    }

    createEvent(data: EventRequest): Observable<Event> {
        return this.http.post<Event>(this.url, data);
    }

    updateEvent(id: number, data: UpdateEventRequest): Observable<Event> {
        return this.http.patch<Event>(`${this.url}/${id}`, data);
    }

    updateEventState(id: number, state: EventStatus): Observable<Event> {
        return this.http.patch<Event>(`${this.url}/${id}/state`, null, {
            params: { state }
        });
    }

    registerToEvent(eventId: number): Observable<Registration> {
        return this.http.post<Registration>(`${this.url}/${eventId}/registrations`, null);
    }

    cancelRegistration(eventId: number, registrationId: number): Observable<Registration> {
        return this.http.patch<Registration>(`${this.url}/${eventId}/registrations/${registrationId}`, null);
    }
}
