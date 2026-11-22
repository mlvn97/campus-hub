import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map, Observable } from 'rxjs';

import { AssociationService } from '../../core/services/association.service';
import { EventService } from '../../core/services/event.service';
import { NewsService } from '../../core/services/news.service';
import { AssociationCategory } from '../../models/association.model';
import { EventStatus } from '../../models/event.model';

type Section = 'associations' | 'evenements' | 'actualites';

interface DirectoryItem {
    id: number;
    title: string;
    description: string | null;
    meta: string | null;
}

interface DirectoryPage {
    items: DirectoryItem[];
    totalPages: number;
}

@Component({
    selector: 'app-directory',
    imports: [FormsModule, RouterLink],
    templateUrl: './directory.component.html',
    styleUrl: './directory.component.css'
})
export class DirectoryComponent {
    private readonly associations = inject(AssociationService);
    private readonly events = inject(EventService);
    private readonly news = inject(NewsService);
    private readonly destroyRef = inject(DestroyRef);

    protected readonly section = inject(ActivatedRoute).snapshot.data['section'] as Section;
    protected readonly title = {
        associations: 'Associations',
        evenements: 'Événements',
        actualites: 'Actualités'
    }[this.section];

    protected readonly items = signal<DirectoryItem[]>([]);
    protected readonly page = signal(1);
    protected readonly totalPages = signal(0);
    protected readonly loading = signal(true);
    protected readonly error = signal<number | null>(null);
    protected associationCategory: AssociationCategory | '' = '';
    protected eventStatus: EventStatus | '' = '';
    protected newsCategory = '';
    protected associationId: number | null = null;

    constructor() {
        afterNextRender(() => this.load(1));
    }

    protected load(page: number): void {
        this.loading.set(true);
        this.error.set(null);
        this.fetchPage(page).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: result => {
                this.items.set(result.items);
                this.page.set(page);
                this.totalPages.set(result.totalPages);
                this.loading.set(false);
            },
            error: (error: HttpErrorResponse) => {
                this.error.set(error.status);
                this.loading.set(false);
            }
        });
    }

    private fetchPage(page: number): Observable<DirectoryPage> {
        switch (this.section) {
            case 'associations':
                return this.associations.fetchAssociations({
                    page: page - 1, size: 10, category: this.associationCategory || undefined
                }).pipe(map(result => ({
                    totalPages: result.totalPages,
                    items: result.content.map(item => ({
                        id: item.id, title: item.name, description: item.description, meta: item.category
                    }))
                })));
            case 'evenements':
                return this.events.fetchEvents({
                    page: page - 1, size: 10, status: this.eventStatus || undefined,
                    associationId: this.associationId ?? undefined
                }).pipe(map(result => ({
                    totalPages: result.totalPages,
                    items: result.content.map(item => ({
                        id: item.id, title: item.title, description: item.description,
                        meta: `${item.date.replace('T', ' ').slice(0, 16)} · ${item.location}`
                    }))
                })));
            case 'actualites':
                return this.news.fetchFeed({
                    page, limit: 10, category: this.newsCategory.trim() || undefined,
                    associationId: this.associationId ?? undefined
                }).pipe(map(result => ({
                    totalPages: result.totalPages,
                    items: result.items.map(item => ({
                        id: item.id, title: item.title, description: item.content,
                        meta: item.publishedAt ? new Date(item.publishedAt).toLocaleDateString('fr-FR') : null
                    }))
                })));
        }
    }
}
