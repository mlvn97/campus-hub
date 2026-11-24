import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { catchError, EMPTY, expand, forkJoin, from, map, mergeMap, Observable, of, reduce, switchMap, toArray } from 'rxjs';

import { AssociationService } from '../../core/services/association.service';
import { AuthService } from '../../core/services/auth.service';
import { EventFilters, EventService } from '../../core/services/event.service';
import { MembershipService } from '../../core/services/membership.service';
import { Association } from '../../models/association.model';
import { Event, EventStatus, Registration } from '../../models/event.model';
import { EventRequest, UpdateEventRequest } from '../../dto/event/event-request.dto';

const previewAssociations = [
    { id: 1, name: 'Club photo' }, { id: 2, name: 'Bureau des sports' }, { id: 3, name: 'Club tech' }
];
const previewEvents: Event[] = [
    { id: 1, associationId: 1, title: 'Atelier photo', date: '2026-11-26T18:00:00',
        location: 'Salle des associations', status: 'POSTED', capacity: 20, description: 'Une initiation à la photo sur le campus.' },
    { id: 2, associationId: 2, title: 'Tournoi du campus', date: '2026-11-27T12:00:00',
        location: 'Gymnase universitaire', status: 'POSTED', capacity: 40, description: 'Un tournoi amical entre étudiants.' },
    { id: 3, associationId: 3, title: 'Atelier code', date: '2026-11-28T14:00:00',
        location: 'Salle informatique', status: 'POSTED', capacity: 16, description: 'Un atelier de programmation en groupe.' }
];
const pageSize = 3;

@Component({
    selector: 'app-events',
    imports: [FormsModule, RouterLink],
    templateUrl: './events.component.html',
    styleUrl: './events.component.css'
})
export class EventsComponent {
    private readonly events = inject(EventService);
    private readonly associations = inject(AssociationService);
    private readonly memberships = inject(MembershipService);
    private readonly auth = inject(AuthService);
    private readonly destroyRef = inject(DestroyRef);
    private filters: EventFilters = {};
    protected readonly items = signal<Event[]>([]);
    protected readonly associationOptions = signal<Pick<Association, 'id' | 'name'>[]>([]);
    protected readonly presidentAssociations = signal<Pick<Association, 'id' | 'name'>[]>([]);
    protected readonly associationError = signal(false);
    protected readonly presidentLookupError = signal(false);
    protected readonly presidentLookupComplete = signal(false);
    protected readonly authenticated = signal(false);
    protected readonly registrations = signal<Record<number, Registration | null>>({});
    protected readonly registrationLookupError = signal(false);
    protected readonly registrationActionError = signal('');
    protected readonly registeringEventId = signal<number | null>(null);
    protected readonly createMessage = signal('');
    protected readonly createError = signal('');
    protected readonly creating = signal(false);
    protected readonly selectedEvent = signal<Event | null>(null);
    protected eventDraft: EventRequest = this.emptyEvent();
    protected readonly savingEvent = signal(false);
    protected readonly eventMutationMessage = signal('');
    protected readonly eventMutationError = signal('');
    protected readonly confirmation = signal<{ message: string; action: () => void } | null>(null);
    protected readonly page = signal(0);
    protected readonly totalPages = signal(0);
    protected readonly loading = signal(true);
    protected readonly preview = signal(false);
    protected readonly error = signal<number | null>(null);
    protected readonly statusLabels: Record<EventStatus, string> = {
        DRAFT: 'Brouillon',
        POSTED: 'Publié',
        COMPLETE: 'Complet',
        OVER: 'Terminé',
        CANCELLED: 'Annulé'
    };
    protected status: EventStatus | '' = '';
    protected associationId: number | null = null;
    protected newEvent: EventRequest = this.emptyEvent();

    constructor() {
        afterNextRender(() => {
            this.authenticated.set(this.auth.isAuthenticated());
            this.fetchAssociations(0).pipe(
                expand(result => result.last ? EMPTY : this.fetchAssociations(result.number + 1)),
                reduce((items, result) => items.concat(result.content), [] as Association[]),
                takeUntilDestroyed(this.destroyRef)
            ).subscribe({
                next: items => {
                    this.associationOptions.set(items);
                    this.loadPresidentAssociations(items);
                },
                error: () => this.associationError.set(true)
            });
            this.load(0);
        });
    }

    protected applyFilters(): void {
        this.filters = { status: this.status || undefined, associationId: this.associationId ?? undefined };
        this.load(0);
    }

    protected load(page: number): void {
        this.page.set(page);
        this.error.set(null);
        this.loading.set(true);
        this.events.fetchEvents({ ...this.filters, page, size: pageSize, sort: 'date,asc' })
            .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
                next: result => {
                    this.preview.set(false);
                    this.items.set(result.content);
                    this.totalPages.set(result.totalPages);
                    this.loadRegistrationStates(result.content);
                },
                error: (error: HttpErrorResponse) => {
                    this.preview.set(error.status === 0);
                    if (this.preview()) {
                        const matches = previewEvents.filter(event =>
                            (!this.filters.status || event.status === this.filters.status)
                            && (!this.filters.associationId || event.associationId === this.filters.associationId));
                        this.items.set(matches.slice(page * pageSize, (page + 1) * pageSize));
                        this.totalPages.set(Math.ceil(matches.length / pageSize));
                    } else {
                        this.error.set(error.status);
                    }
                    this.loading.set(false);
                }
            });
    }

    protected options() {
        return this.preview() ? previewAssociations : this.associationOptions();
    }

    protected openCreateDialog(dialog: HTMLDialogElement): void {
        const associations = this.presidentAssociations();
        this.newEvent = {
            ...this.emptyEvent(),
            associationId: associations.length === 1 ? associations[0].id : 0
        };
        this.createError.set('');
        this.createMessage.set('');
        dialog.showModal();
    }

    protected canManageEvent(event: Event): boolean {
        return this.presidentAssociations().some(association => association.id === event.associationId);
    }

    protected openManageEventDialog(event: Event, dialog: HTMLDialogElement): void {
        this.selectedEvent.set(event);
        this.eventDraft = {
            associationId: event.associationId,
            title: event.title,
            description: event.description ?? '',
            date: event.date.slice(0, 16),
            location: event.location,
            capacity: event.capacity,
            status: event.status
        };
        this.eventMutationMessage.set('');
        this.eventMutationError.set('');
        dialog.showModal();
    }

    protected saveEventDraft(form: NgForm, dialog: HTMLDialogElement): void {
        const event = this.selectedEvent();
        if (!event || event.status !== 'DRAFT' || this.savingEvent() || form.invalid) {
            return;
        }

        this.savingEvent.set(true);
        this.eventMutationError.set('');
        this.events.updateEvent(event.id, this.eventUpdateRequest())
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: updated => {
                    this.finishEventUpdate(updated, 'Les modifications ont été enregistrées en brouillon.', dialog);
                },
                error: error => this.handleEventMutationError(error)
            });
    }

    protected publishEvent(form: NgForm, dialog: HTMLDialogElement): void {
        const event = this.selectedEvent();
        if (!event || event.status !== 'DRAFT' || this.savingEvent() || form.invalid) {
            return;
        }

        this.savingEvent.set(true);
        this.eventMutationError.set('');
        this.events.updateEvent(event.id, this.eventUpdateRequest()).pipe(
            switchMap(() => this.events.updateEventState(event.id, 'POSTED')),
            takeUntilDestroyed(this.destroyRef)
        ).subscribe({
            next: updated => this.finishEventUpdate(updated, 'L’événement a été publié.', dialog),
            error: error => this.handleEventMutationError(error)
        });
    }

    protected cancelEvent(dialog: HTMLDialogElement, confirmationDialog: HTMLDialogElement): void {
        const event = this.selectedEvent();
        if (!event || event.status === 'OVER' || event.status === 'CANCELLED' || this.savingEvent()) {
            return;
        }

        this.confirmation.set({
            message: `Annuler l’événement « ${event.title} » ?`,
            action: () => this.performCancelEvent(event, dialog)
        });
        confirmationDialog.showModal();
    }

    protected confirmPendingAction(dialog: HTMLDialogElement): void {
        const pending = this.confirmation();
        if (!pending) {
            return;
        }
        this.confirmation.set(null);
        dialog.close();
        pending.action();
    }

    protected clearConfirmation(): void {
        this.confirmation.set(null);
    }

    private performCancelEvent(event: Event, dialog: HTMLDialogElement): void {
        this.savingEvent.set(true);
        this.eventMutationError.set('');
        this.events.updateEventState(event.id, 'CANCELLED')
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: updated => this.finishEventUpdate(updated, 'L’événement a été annulé.', dialog),
                error: error => this.handleEventMutationError(error)
            });
    }

    protected createEvent(form: NgForm, dialog: HTMLDialogElement): void {
        if (this.creating() || form.invalid || this.newEvent.associationId <= 0) {
            return;
        }

        this.creating.set(true);
        this.createError.set('');
        this.events.createEvent(this.newEvent)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: event => {
                    this.creating.set(false);
                    dialog.close();
                    this.createMessage.set(`L’événement « ${event.title} » a été créé.`);
                    this.load(0);
                },
                error: (error: HttpErrorResponse) => {
                    this.creating.set(false);
                    this.createError.set(error.status === 401 || error.status === 403
                        ? 'Tu dois être président de cette association pour créer un événement.'
                        : 'Impossible de créer l’événement. Vérifie les informations ou réessaie.');
                }
            });
    }

    protected toggleRegistration(event: Event): void {
        if (this.registeringEventId() !== null) {
            return;
        }
        if (!this.authenticated()) {
            this.registrationActionError.set('Connecte-toi pour t’inscrire à un événement.');
            return;
        }

        const registration = this.registrations()[event.id];
        this.registeringEventId.set(event.id);
        this.registrationActionError.set('');
        const action: Observable<Registration | null> = registration
            ? this.events.cancelRegistration(event.id, registration.id).pipe(map(() => null))
            : this.events.registerToEvent(event.id);
        action.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: updatedRegistration => {
                this.registrations.update(current => ({ ...current, [event.id]: updatedRegistration }));
                this.registeringEventId.set(null);
            },
            error: (error: HttpErrorResponse) => {
                this.registeringEventId.set(null);
                this.registrationActionError.set(error.status === 401
                    ? 'Connecte-toi pour gérer ton inscription.'
                    : error.status === 409
                        ? 'Événement indisponible, déjà passé ou inscription déjà enregistrée.'
                        : 'Impossible de modifier ton inscription. Réessaie plus tard.');
            }
        });
    }

    protected canRegister(event: Event): boolean {
        return this.presidentLookupComplete()
            && !this.presidentLookupError()
            && (event.status === 'POSTED' || event.status === 'COMPLETE')
            && !this.isPresidentOfEvent(event);
    }

    protected isPresidentOfEvent(event: Event): boolean {
        return this.presidentAssociations().some(association => association.id === event.associationId);
    }

    protected statusClass(status: EventStatus): string {
        return status.toLowerCase();
    }

    protected dateLabel(date: string): string {
        const value = new Date(date);
        const day = new Intl.DateTimeFormat('fr-FR', { weekday: 'long' }).format(value);
        const time = value.getMinutes() ? ':' + String(value.getMinutes()).padStart(2, '0') : ' h';
        return day.charAt(0).toUpperCase() + day.slice(1) + ' · ' + value.getHours() + time;
    }

    private fetchAssociations(page: number) {
        return this.associations.fetchAssociations({ page, size: 50 });
    }

    private loadPresidentAssociations(associations: Association[]): void {
        this.presidentAssociations.set([]);
        this.presidentLookupError.set(false);
        this.presidentLookupComplete.set(false);
        if (!this.authenticated() || associations.length === 0) {
            this.presidentLookupComplete.set(true);
            return;
        }

        from(associations).pipe(
            mergeMap(association => this.memberships.isPresident(association.id).pipe(
                map(isPresident => isPresident ? association : null),
                catchError(() => {
                    this.presidentLookupError.set(true);
                    return of(null);
                })
            ), 6),
            toArray(),
            takeUntilDestroyed(this.destroyRef)
        ).subscribe({
            next: results => {
                this.presidentAssociations.set(
                    results.filter((association): association is Association => association !== null)
                );
                this.presidentLookupComplete.set(true);
            }
        });
    }

    private loadRegistrationStates(events: Event[]): void {
        this.registrations.set({});
        this.registrationLookupError.set(false);
        if (!this.authenticated() || events.length === 0) {
            this.loading.set(false);
            return;
        }

        forkJoin(events.map(event => this.events.fetchMyRegistration(event.id).pipe(
            catchError(() => {
                this.registrationLookupError.set(true);
                return of(null);
            })
        ))).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: registrations => {
                this.registrations.set(Object.fromEntries(
                    events.map((event, index) => [event.id, registrations[index]])
                ));
                this.loading.set(false);
            },
            error: () => {
                this.registrationLookupError.set(true);
                this.loading.set(false);
            }
        });
    }

    private emptyEvent(): EventRequest {
        return {
            associationId: 0,
            title: '',
            description: '',
            date: '',
            location: '',
            capacity: 1,
            status: 'DRAFT'
        };
    }

    private eventUpdateRequest(): UpdateEventRequest {
        const draft = this.eventDraft;
        return {
            associationId: draft.associationId,
            title: draft.title,
            description: draft.description,
            date: draft.date,
            location: draft.location,
            capacity: draft.capacity
        };
    }

    private finishEventUpdate(updated: Event, message: string, dialog: HTMLDialogElement): void {
        this.savingEvent.set(false);
        this.items.update(events => events.map(event => event.id === updated.id ? updated : event));
        this.eventMutationMessage.set(message);
        dialog.close();
        this.load(this.page());
    }

    private handleEventMutationError(error: HttpErrorResponse): void {
        this.savingEvent.set(false);
        this.eventMutationError.set(error.status === 401 || error.status === 403
            ? 'Tu dois être président de l’association pour gérer cet événement.'
            : error.status === 409
                ? 'Seuls les événements brouillons peuvent être modifiés.'
                : 'Impossible de modifier l’événement. Vérifie les informations ou réessaie.');
    }
}
