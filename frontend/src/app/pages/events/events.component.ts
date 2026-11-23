import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EMPTY, expand, reduce } from 'rxjs';

import { AssociationService } from '../../core/services/association.service';
import { EventFilters, EventService } from '../../core/services/event.service';
import { Association } from '../../models/association.model';
import { Event, EventStatus } from '../../models/event.model';

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
    private readonly destroyRef = inject(DestroyRef);
    private filters: EventFilters = {};
    protected readonly items = signal<Event[]>([]);
    protected readonly associationOptions = signal<Pick<Association, 'id' | 'name'>[]>([]);
    protected readonly associationError = signal(false);
    protected readonly page = signal(0);
    protected readonly totalPages = signal(0);
    protected readonly loading = signal(true);
    protected readonly preview = signal(false);
    protected readonly error = signal<number | null>(null);
    protected status: EventStatus | '' = '';
    protected associationId: number | null = null;

    constructor() {
        afterNextRender(() => {
            this.fetchAssociations(0).pipe(
                expand(result => result.last ? EMPTY : this.fetchAssociations(result.number + 1)),
                reduce((items, result) => items.concat(result.content), [] as Association[]),
                takeUntilDestroyed(this.destroyRef)
            ).subscribe({
                next: items => this.associationOptions.set(items),
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
                    this.loading.set(false);
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

    protected dateLabel(date: string): string {
        const value = new Date(date);
        const day = new Intl.DateTimeFormat('fr-FR', { weekday: 'long' }).format(value);
        const time = value.getMinutes() ? ':' + String(value.getMinutes()).padStart(2, '0') : ' h';
        return day.charAt(0).toUpperCase() + day.slice(1) + ' · ' + value.getHours() + time;
    }

    private fetchAssociations(page: number) {
        return this.associations.fetchAssociations({ page, size: 50 });
    }
}
