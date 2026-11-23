import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AssociationFilters, AssociationService } from '../../core/services/association.service';
import { Association, AssociationCategory, AssociationStatus } from '../../models/association.model';

const examples: Association[] = [
    { id: 1, name: 'Club photo', category: 'CULTURE', status: 'ACTIVE',
        description: 'Explore la photographie et partage tes clichés.' },
    { id: 2, name: 'Bureau des sports', category: 'SPORT', status: 'ACTIVE',
        description: 'Participe aux entraînements et aux tournois du campus.' },
    { id: 3, name: 'Club tech', category: 'TECH', status: 'ACTIVE',
        description: 'Développe des projets avec d’autres passionnés.' }
];
const pageSize = 3;

@Component({
    selector: 'app-directory',
    imports: [FormsModule, RouterLink],
    templateUrl: './directory.component.html',
    styleUrl: './directory.component.css'
})
export class DirectoryComponent {
    private readonly associations = inject(AssociationService);
    private readonly destroyRef = inject(DestroyRef);
    private filters: AssociationFilters = {};
    protected readonly items = signal<Association[]>([]);
    protected readonly page = signal(0);
    protected readonly totalPages = signal(0);
    protected readonly loading = signal(true);
    protected readonly error = signal<number | null>(null);
    protected readonly preview = signal(false);
    protected readonly categories: Record<AssociationCategory, string> = {
        CULTURE: 'Culture', SPORT: 'Sport', TECH: 'Technologie',
        HUMANITARIAN: 'Humanitaire', OTHER: 'Autre'
    };
    protected readonly categoryKeys = Object.keys(this.categories) as AssociationCategory[];
    protected category: AssociationCategory | '' = '';
    protected status: AssociationStatus | '' = '';

    constructor() {
        afterNextRender(() => this.load(0));
    }

    protected applyFilters(): void {
        this.filters = { category: this.category || undefined, status: this.status || undefined };
        this.load(0);
    }

    protected load(page: number): void {
        this.page.set(page);
        this.error.set(null);
        this.loading.set(true);
        this.associations.fetchAssociations({ ...this.filters, page, size: pageSize })
            .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
                next: result => {
                    this.items.set(result.content);
                    this.totalPages.set(result.totalPages);
                    this.preview.set(false);
                    this.loading.set(false);
                },
                error: (error: HttpErrorResponse) => {
                    this.preview.set(error.status === 0);
                    if (this.preview()) {
                        const matches = examples.filter(item =>
                            (!this.filters.category || item.category === this.filters.category)
                            && (!this.filters.status || item.status === this.filters.status));
                        this.items.set(matches.slice(page * pageSize, (page + 1) * pageSize));
                        this.totalPages.set(Math.ceil(matches.length / pageSize));
                    } else {
                        this.error.set(error.status);
                    }
                    this.loading.set(false);
                }
            });
    }
}
