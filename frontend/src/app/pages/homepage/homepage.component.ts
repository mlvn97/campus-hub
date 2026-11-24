import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { AssociationService } from '../../core/services/association.service';
import { EventService } from '../../core/services/event.service';
import { NewsService } from '../../core/services/news.service';
import { Association, AssociationCategory } from '../../models/association.model';
import { Event } from '../../models/event.model';
import { News } from '../../models/news.model';

@Component({
    selector: 'app-homepage',
    imports: [RouterLink],
    templateUrl: './homepage.component.html',
    styleUrl: './homepage.component.css'
})
export class HomepageComponent {

    private readonly newsService: NewsService = inject(NewsService);
    private readonly eventService: EventService = inject(EventService);
    private readonly associationService: AssociationService = inject(AssociationService);
    private readonly destroyRef: DestroyRef = inject(DestroyRef);

    protected readonly newsList = signal<News[]>([]);
    protected readonly loadingNews = signal(true);
    protected readonly newsError = signal<number | null>(null);
    protected readonly eventList = signal<Event[]>([]);
    protected readonly loadingEvents = signal(true);
    protected readonly eventsError = signal<number | null>(null);
    protected readonly associationList = signal<Association[]>([]);
    protected readonly loadingAssociations = signal(true);
    protected readonly associationsError = signal<number | null>(null);
    private readonly categoryLabels: Record<AssociationCategory, string> = {
        CULTURE: 'Culture',
        SPORT: 'Sport',
        TECH: 'Technologie',
        HUMANITARIAN: 'Humanitaire',
        OTHER: 'Autre'
    };
    private readonly categoryIcons: Record<AssociationCategory, string> = {
        CULTURE: 'bi bi-music-note-beamed',
        SPORT: 'bi bi-trophy',
        TECH: 'bi bi-code-slash',
        HUMANITARIAN: 'bi bi-heart',
        OTHER: 'bi bi-people'
    };

    constructor() {
        afterNextRender(() => {
            this.getNewsFeed();
            this.getEvents();
            this.getAssociations();
        });
    }

    protected getNewsFeed(): void {
        this.loadingNews.set(true);
        this.newsError.set(null);
        this.newsService.fetchFeed({ page: 1, limit: 2 })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: feed => {
                    this.newsList.set(feed.items.filter(item => item.status === 'PUBLIEE'));
                    this.loadingNews.set(false);
                },
                error: (error: HttpErrorResponse) => {
                    this.newsError.set(error.status);
                    this.loadingNews.set(false);
                }
            });
    }

    protected getEvents(): void {
        this.loadingEvents.set(true);
        this.eventsError.set(null);
        this.eventService.fetchEvents({ page: 0, size: 2, status: 'POSTED', sort: 'date,asc' })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: result => {
                    this.eventList.set(result.content);
                    this.loadingEvents.set(false);
                },
                error: (error: HttpErrorResponse) => {
                    this.eventsError.set(error.status);
                    this.loadingEvents.set(false);
                }
            });
    }

    protected getAssociations(): void {
        this.loadingAssociations.set(true);
        this.associationsError.set(null);
        this.associationService.fetchAssociations({
            page: 0, size: 3, status: 'ACTIVE', sort: 'name,asc'
        }).pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: result => {
                    this.associationList.set(result.content);
                    this.loadingAssociations.set(false);
                },
                error: (error: HttpErrorResponse) => {
                    this.associationsError.set(error.status);
                    this.loadingAssociations.set(false);
                }
            });
    }

    protected eventDay(date: string): string {
        return new Intl.DateTimeFormat('fr-FR', { day: '2-digit' }).format(new Date(date));
    }

    protected eventMonth(date: string): string {
        return new Intl.DateTimeFormat('fr-FR', { month: 'short' }).format(new Date(date)).toUpperCase();
    }

    protected associationCategory(category: AssociationCategory): string {
        return this.categoryLabels[category];
    }

    protected associationIcon(category: AssociationCategory): string {
        return this.categoryIcons[category];
    }
}
