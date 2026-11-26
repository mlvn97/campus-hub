import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { distinctUntilChanged, map, Subscription } from 'rxjs';

import { AssociationService } from '../../core/services/association.service';
import { NewsService } from '../../core/services/news.service';
import { Association } from '../../models/association.model';
import { News, NewsStatus } from '../../models/news.model';

@Component({
    selector: 'app-news-detail',
    imports: [RouterLink],
    templateUrl: './news-detail.component.html',
    styleUrl: './news-detail.component.css'
})
export class NewsDetailComponent {
    private readonly route = inject(ActivatedRoute);
    private readonly newsService = inject(NewsService);
    private readonly associationService = inject(AssociationService);
    private readonly destroyRef = inject(DestroyRef);
    private detailRequest?: Subscription;
    private associationRequest?: Subscription;
    private currentId: string | null = null;

    protected readonly item = signal<News | null>(null);
    protected readonly association = signal<Association | null>(null);
    protected readonly loading = signal(true);
    protected readonly error = signal<number | null>(null);
    protected readonly associationError = signal(false);

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

    protected publishedDate(value: string): string {
        const date = new Date(value);
        return Number.isNaN(date.getTime())
            ? value
            : new Intl.DateTimeFormat('fr-FR', { dateStyle: 'long', timeStyle: 'short' }).format(date);
    }

    protected statusLabel(status: NewsStatus): string {
        return status === 'BROUILLON' ? 'Brouillon' : status === 'PUBLIEE' ? 'Publiée' : 'Archivée';
    }

    private load(rawId: string | null): void {
        this.currentId = rawId;
        this.detailRequest?.unsubscribe();
        this.associationRequest?.unsubscribe();
        this.item.set(null);
        this.association.set(null);
        this.associationError.set(false);
        this.error.set(null);

        const id = Number(rawId);
        if (!rawId || !Number.isSafeInteger(id) || id < 1) {
            this.loading.set(false);
            this.error.set(404);
            return;
        }

        this.loading.set(true);
        this.detailRequest = this.newsService.fetchNews(id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: item => {
                    this.item.set(item);
                    this.loading.set(false);
                    if (item.associationId !== null) {
                        this.loadAssociation(item.associationId);
                    }
                },
                error: (error: HttpErrorResponse) => {
                    this.loading.set(false);
                    this.error.set(error.status);
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
