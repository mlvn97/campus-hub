import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { distinctUntilChanged, map, Observable, Subscription } from 'rxjs';

import { AssociationService } from '../../core/services/association.service';
import { AuthService } from '../../core/services/auth.service';
import { EventService } from '../../core/services/event.service';
import { MembershipService } from '../../core/services/membership.service';
import { Association } from '../../models/association.model';
import { Event, EventStatus, Registration } from '../../models/event.model';

@Component({
    selector: 'app-event-detail',
    imports: [RouterLink],
    templateUrl: './event-detail.component.html',
    styleUrl: './event-detail.component.css'
})
export class EventDetailComponent {
    private readonly route = inject(ActivatedRoute);
    private readonly eventService = inject(EventService);
    private readonly associationService = inject(AssociationService);
    private readonly auth = inject(AuthService);
    private readonly memberships = inject(MembershipService);
    private readonly destroyRef = inject(DestroyRef);
    private detailRequest?: Subscription;
    private associationRequest?: Subscription;
    private presidentRequest?: Subscription;
    private registrationRequest?: Subscription;
    private registrationActionRequest?: Subscription;
    private currentId: string | null = null;

    protected readonly item = signal<Event | null>(null);
    protected readonly association = signal<Association | null>(null);
    protected readonly loading = signal(true);
    protected readonly error = signal<number | null>(null);
    protected readonly associationError = signal(false);
    protected readonly authenticated = signal(false);
    protected readonly isPresident = signal(false);
    protected readonly presidentCheckLoading = signal(false);
    protected readonly presidentCheckError = signal(false);
    protected readonly registration = signal<Registration | null>(null);
    protected readonly registrationLoading = signal(false);
    protected readonly registrationError = signal('');
    protected readonly registrationNeedsLogin = signal(false);
    protected readonly registrationActionLoading = signal(false);
    protected readonly registrationActionMessage = signal('');
    protected readonly registrationActionError = signal('');
    protected readonly registrationActionNeedsLogin = signal(false);

    private readonly statuses: Record<EventStatus, string> = {
        DRAFT: 'Brouillon',
        POSTED: 'Publié',
        COMPLETE: 'Complet',
        OVER: 'Terminé',
        CANCELLED: 'Annulé'
    };

    constructor() {
        afterNextRender(() => {
            this.route.paramMap.pipe(
                map(params => params.get('id')),
                distinctUntilChanged(),
                takeUntilDestroyed(this.destroyRef)
            ).subscribe(id => this.load(id));
        });
    }

    protected retry(): void {
        this.load(this.currentId);
    }

    protected statusLabel(status: EventStatus): string {
        return this.statuses[status];
    }

    protected eventDate(value: string): string {
        const date = new Date(value);
        return Number.isNaN(date.getTime())
            ? value
            : new Intl.DateTimeFormat('fr-FR', { dateStyle: 'full', timeStyle: 'short' }).format(date);
    }

    protected canRegister(): boolean {
        const event = this.item();
        return !!event && (event.status === 'POSTED' || event.status === 'COMPLETE')
            && !this.isPresident();
    }

    protected toggleRegistration(): void {
        const event = this.item();
        const registration = this.registration();
        const activeRegistration = registration?.status === 'CANCELLED' ? null : registration;
        if (!event || !this.authenticated() || this.registrationActionLoading()
            || (!activeRegistration && (this.presidentCheckLoading() || this.presidentCheckError()))) {
            return;
        }

        this.registrationActionLoading.set(true);
        this.registrationActionMessage.set('');
        this.registrationActionError.set('');
        this.registrationActionNeedsLogin.set(false);
        const action: Observable<Registration | null> = activeRegistration
            ? this.eventService.cancelRegistration(event.id, activeRegistration.id).pipe(map(() => null))
            : this.eventService.registerToEvent(event.id);
        this.registrationActionRequest = action.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: updated => {
                this.registration.set(updated);
                this.registrationActionLoading.set(false);
                this.registrationActionMessage.set(updated
                    ? updated.status === 'PENDING' ? 'Ta demande est en attente de confirmation.' : 'Tu es inscrit à cet événement.'
                    : 'Tu as annulé ton inscription.');
            },
            error: (error: HttpErrorResponse) => {
                this.registrationActionLoading.set(false);
                this.registrationActionNeedsLogin.set(error.status === 401);
                this.registrationActionError.set(error.status === 401
                    ? 'Connecte-toi pour gérer ton inscription.'
                    : error.status === 409
                        ? 'Événement indisponible, déjà passé ou inscription déjà enregistrée.'
                        : 'Impossible de modifier ton inscription. Réessaie plus tard.');
            }
        });
    }

    private load(rawId: string | null): void {
        this.currentId = rawId;
        this.detailRequest?.unsubscribe();
        this.associationRequest?.unsubscribe();
        this.presidentRequest?.unsubscribe();
        this.registrationRequest?.unsubscribe();
        this.registrationActionRequest?.unsubscribe();
        this.item.set(null);
        this.association.set(null);
        this.associationError.set(false);
        this.error.set(null);
        this.authenticated.set(this.auth.isAuthenticated());
        this.isPresident.set(false);
        this.presidentCheckLoading.set(false);
        this.presidentCheckError.set(false);
        this.registration.set(null);
        this.registrationLoading.set(false);
        this.registrationError.set('');
        this.registrationNeedsLogin.set(false);
        this.registrationActionLoading.set(false);
        this.registrationActionMessage.set('');
        this.registrationActionError.set('');
        this.registrationActionNeedsLogin.set(false);

        const id = Number(rawId);
        if (!rawId || !Number.isSafeInteger(id) || id < 1) {
            this.loading.set(false);
            this.error.set(404);
            return;
        }

        this.loading.set(true);
        this.detailRequest = this.eventService.fetchEvent(id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: item => {
                    this.item.set(item);
                    this.loading.set(false);
                    this.loadAssociation(item.associationId);
                    this.loadRegistration(item);
                    this.checkPresident(item.associationId);
                },
                error: (error: HttpErrorResponse) => {
                    this.loading.set(false);
                    this.error.set(error.status);
                }
            });
    }

    private loadRegistration(event: Event): void {
        if (!this.authenticated()) {
            return;
        }

        this.registrationLoading.set(true);
        this.registrationRequest = this.eventService.fetchMyRegistration(event.id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: registration => {
                    this.registration.set(registration);
                    this.registrationLoading.set(false);
                },
                error: (error: HttpErrorResponse) => {
                    this.registrationLoading.set(false);
                    this.registrationNeedsLogin.set(error.status === 401);
                    this.registrationError.set(error.status === 401
                        ? 'Ta session a expiré. Reconnecte-toi pour gérer ton inscription.'
                        : 'Impossible de vérifier ton inscription. Réessaie plus tard.');
                }
            });
    }

    private checkPresident(associationId: number): void {
        if (!this.authenticated()) {
            return;
        }

        this.presidentCheckLoading.set(true);
        this.presidentRequest = this.memberships.isPresident(associationId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: isPresident => {
                    this.isPresident.set(isPresident);
                    this.presidentCheckLoading.set(false);
                },
                error: () => {
                    this.presidentCheckError.set(true);
                    this.presidentCheckLoading.set(false);
                }
            });
    }

    private loadAssociation(id: number): void {
        this.associationRequest = this.associationService.fetchAssociation(id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: association => this.association.set(association),
                error: () => this.associationError.set(true)
            });
    }
}
