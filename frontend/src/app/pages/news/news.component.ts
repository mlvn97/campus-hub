import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EMPTY, expand, reduce } from 'rxjs';

import { AssociationService } from '../../core/services/association.service';
import { NewsFilters, NewsService } from '../../core/services/news.service';
import { Association } from '../../models/association.model';
import { News } from '../../models/news.model';

const pageSize = 3;
const examples: News[] = [
    {
        id: 1, title: 'Forum des associations', category: 'Vie étudiante', associationId: null,
        status: 'PUBLIEE', publishedAt: null,
        content: 'Découvrez les associations du campus, échangez avec leurs membres et trouvez celle qui vous correspond lors du forum annuel.'
    },
    {
        id: 2, title: 'La semaine du sport', category: 'Sport', associationId: 1,
        status: 'PUBLIEE', publishedAt: '2023-09-12T12:00:00Z',
        content: 'Une semaine d’initiations, de tournois et de rencontres pour bouger ensemble sur le campus.'
    },
    {
        id: 3, title: 'Un nouvel espace de travail', category: 'Vie étudiante', associationId: 2,
        status: 'PUBLIEE', publishedAt: '2023-09-08T12:00:00Z',
        content: 'Un espace moderne, calme et collaboratif ouvre ses portes au cœur du campus pour vos projets et révisions.'
    }
];
const exampleAssociations = [{ id: 1, name: 'Bureau des sports' }, { id: 2, name: 'Club tech' }];
const dateFormatter = new Intl.DateTimeFormat('fr-FR', {
    weekday: 'short', day: 'numeric', month: 'short', timeZone: 'Europe/Paris'
});

@Component({
    selector: 'app-news',
    imports: [FormsModule, RouterLink],
    templateUrl: './news.component.html',
    styleUrl: './news.component.css'
})
export class NewsComponent {
    private readonly news = inject(NewsService);
    private readonly associations = inject(AssociationService);
    private readonly destroyRef = inject(DestroyRef);
    private filters: NewsFilters = {};
    protected readonly items = signal<News[]>([]);
    protected readonly categoryOptions = signal<string[]>([]);
    protected readonly associationsList = signal<Pick<Association, 'id' | 'name'>[]>([]);
    protected readonly associationError = signal(false);
    protected readonly page = signal(1);
    protected readonly totalPages = signal(0);
    protected readonly loading = signal(true);
    protected readonly preview = signal(false);
    protected readonly error = signal<number | null>(null);
    protected readonly selected = signal<News | null>(null);
    protected category = '';
    protected associationId: number | null = null;

    constructor() {
        afterNextRender(() => {
            this.fetchAssociations(0).pipe(
                expand(result => result.last ? EMPTY : this.fetchAssociations(result.number + 1)),
                reduce((items, result) => items.concat(result.content), [] as Association[]),
                takeUntilDestroyed(this.destroyRef)
            ).subscribe({
                next: items => this.associationsList.set(items),
                error: () => this.associationError.set(true)
            });
            this.load(1);
        });
    }

    protected applyFilters(): void {
        this.filters = { category: this.category.trim() || undefined, associationId: this.associationId ?? undefined };
        this.load(1);
    }

    protected load(page: number): void {
        this.page.set(page);
        this.error.set(null);
        this.loading.set(true);
        this.news.fetchFeed({ ...this.filters, page, limit: pageSize })
            .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
                next: result => {
                    this.preview.set(false);
                    this.items.set(result.items);
                    this.totalPages.set(result.totalPages);
                    this.categoryOptions.update(categories => [
                        ...new Set([...categories, ...result.items.map(item => item.category)])
                    ]);
                    this.loading.set(false);
                },
                error: (error: HttpErrorResponse) => {
                    this.preview.set(error.status === 0);
                    if (this.preview()) {
                        const matches = examples.filter(item =>
                            (!this.filters.category || item.category === this.filters.category)
                            && (!this.filters.associationId || item.associationId === this.filters.associationId));
                        this.items.set(matches);
                        this.totalPages.set(matches.length ? 1 : 0);
                    } else {
                        this.error.set(error.status);
                    }
                    this.loading.set(false);
                }
            });
    }

    protected options() {
        return this.preview() ? exampleAssociations : this.associationsList();
    }

    protected categories() {
        return this.preview() ? ['Vie étudiante', 'Sport'] : this.categoryOptions();
    }

    protected read(item: News, dialog: HTMLDialogElement): void {
        this.selected.set(item);
        dialog.showModal();
    }

    protected shortDate(value: string): string {
        const label = dateFormatter.format(new Date(value));
        return label.charAt(0).toUpperCase() + label.slice(1);
    }

    private fetchAssociations(page: number) {
        return this.associations.fetchAssociations({ page, size: 50 });
    }
}
