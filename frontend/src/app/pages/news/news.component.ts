import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { catchError, EMPTY, expand, from, map, mergeMap, Observable, of, reduce, switchMap, toArray } from 'rxjs';

import { AssociationService } from '../../core/services/association.service';
import { AuthService } from '../../core/services/auth.service';
import { MembershipService } from '../../core/services/membership.service';
import { NewsFilters, NewsService } from '../../core/services/news.service';
import { CreateNewsRequest } from '../../dto/news/news-request.dto';
import { Association } from '../../models/association.model';
import { News, NewsStatus } from '../../models/news.model';

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
    private readonly memberships = inject(MembershipService);
    private readonly auth = inject(AuthService);
    private readonly destroyRef = inject(DestroyRef);
    private filters: NewsFilters = {};
    protected readonly items = signal<News[]>([]);
    protected readonly categoryOptions = signal<string[]>([]);
    protected readonly associationsList = signal<Pick<Association, 'id' | 'name'>[]>([]);
    protected readonly associationError = signal(false);
    protected readonly presidentAssociations = signal<Pick<Association, 'id' | 'name'>[]>([]);
    protected readonly presidentLookupError = signal(false);
    protected readonly canManageNews = signal(false);
    protected readonly campusAdmin = signal(false);
    protected readonly managementItems = signal<News[]>([]);
    protected readonly managementLoading = signal(false);
    protected readonly managementError = signal('');
    protected readonly managementPage = signal(1);
    protected readonly managementTotalPages = signal(0);
    protected readonly authenticated = signal(false);
    protected readonly savingNews = signal(false);
    protected readonly newsMessage = signal('');
    protected readonly newsError = signal('');
    protected readonly page = signal(1);
    protected readonly totalPages = signal(0);
    protected readonly loading = signal(true);
    protected readonly preview = signal(false);
    protected readonly error = signal<number | null>(null);
    protected readonly selected = signal<News | null>(null);
    protected readonly editing = signal<News | null>(null);
    protected newsDraft: CreateNewsRequest = this.emptyNews();
    protected readonly confirmation = signal<{ news: News; action: 'archive' | 'delete'; message: string } | null>(null);
    protected category = '';
    protected associationId: number | null = null;

    constructor() {
        afterNextRender(() => {
            this.authenticated.set(this.auth.isAuthenticated());
            this.campusAdmin.set(this.auth.adminCampus());
            if (this.auth.adminCampus()) {
                this.canManageNews.set(true);
                this.loadManagement(1);
            }
            this.fetchAssociations(0).pipe(
                expand(result => result.last ? EMPTY : this.fetchAssociations(result.number + 1)),
                reduce((items, result) => items.concat(result.content), [] as Association[]),
                takeUntilDestroyed(this.destroyRef)
            ).subscribe({
                next: items => {
                    this.associationsList.set(items);
                    this.loadPresidentAssociations(items);
                },
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

    protected openCreateDialog(dialog: HTMLDialogElement): void {
        this.editing.set(null);
        const association = this.presidentAssociations();
        this.newsDraft = {
            ...this.emptyNews(),
            associationId: !this.campusAdmin() && association.length === 1 ? association[0].id : null
        };
        this.newsError.set('');
        this.newsMessage.set('');
        dialog.showModal();
    }

    protected openEditDialog(item: News, dialog: HTMLDialogElement): void {
        this.editing.set(item);
        this.newsDraft = {
            title: item.title,
            content: item.content,
            category: item.category,
            associationId: item.associationId
        };
        this.newsError.set('');
        this.newsMessage.set('');
        dialog.showModal();
    }

    protected saveNews(form: NgForm, dialog: HTMLDialogElement, publish: boolean): void {
        const selected = this.editing();
        if (this.savingNews() || form.invalid || (!selected && !this.canManageNews())
            || (selected && selected.status !== 'BROUILLON')) {
            return;
        }

        this.savingNews.set(true);
        this.newsError.set('');
        const save = selected
            ? this.news.updateNews(selected.id, {
                title: this.newsDraft.title,
                content: this.newsDraft.content,
                category: this.newsDraft.category
            })
            : this.news.createNews(this.newsDraft);
        (publish ? save.pipe(switchMap(item => this.news.publishNews(item.id))) : save)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: item => {
                    this.savingNews.set(false);
                    dialog.close();
                    this.newsMessage.set(publish
                        ? `L’actualité « ${item.title} » a été publiée.`
                        : `Le brouillon « ${item.title} » a été enregistré.`);
                    this.load(1);
                    this.loadManagement(1);
                },
                error: error => this.handleNewsError(error)
            });
    }

    protected requestNewsConfirmation(
        item: News,
        action: 'archive' | 'delete',
        dialog: HTMLDialogElement
    ): void {
        this.newsError.set('');
        this.confirmation.set({
            news: item,
            action,
            message: action === 'archive'
                ? `Archiver l’actualité « ${item.title} » ?`
                : `Supprimer définitivement l’actualité « ${item.title} » ?`
        });
        dialog.showModal();
    }

    protected confirmNewsAction(dialog: HTMLDialogElement): void {
        const pending = this.confirmation();
        if (!pending || this.savingNews()) {
            return;
        }

        this.savingNews.set(true);
        this.newsError.set('');
        const action: Observable<void> = pending.action === 'archive'
            ? this.news.archiveNews(pending.news.id).pipe(map(() => undefined))
            : this.news.deleteNews(pending.news.id);
        action.pipe(
            takeUntilDestroyed(this.destroyRef)
        ).subscribe({
            next: () => {
                this.savingNews.set(false);
                this.confirmation.set(null);
                dialog.close();
                this.newsMessage.set(pending.action === 'archive'
                    ? 'L’actualité a été archivée.'
                    : 'L’actualité a été supprimée.');
                this.load(1);
                this.loadManagement(1);
            },
            error: error => {
                this.savingNews.set(false);
                this.newsError.set(error.status === 403
                    ? 'Tu ne peux gérer que tes propres actualités.'
                    : 'Impossible de modifier cette actualité. Réessaie.');
            }
        });
    }

    protected clearConfirmation(): void {
        this.confirmation.set(null);
    }

    protected loadManagement(page: number): void {
        if (!this.canManageNews()) {
            return;
        }
        this.managementPage.set(page);
        this.managementLoading.set(true);
        this.managementError.set('');
        this.news.fetchManaged({ page, limit: pageSize })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: result => {
                    this.managementItems.set(this.campusAdmin()
                        ? result.items
                        : result.items.filter(item =>
                            this.presidentAssociations().some(association => association.id === item.associationId)));
                    this.managementTotalPages.set(result.totalPages);
                    this.managementLoading.set(false);
                },
                error: error => {
                    this.managementError.set(error.status === 403
                        ? 'Tu ne peux consulter que tes propres actualités.'
                        : 'Impossible de charger les actualités à gérer.');
                    this.managementLoading.set(false);
                }
            });
    }

    protected shortDate(value: string): string {
        const label = dateFormatter.format(new Date(value));
        return label.charAt(0).toUpperCase() + label.slice(1);
    }

    protected statusLabel(status: NewsStatus): string {
        return status === 'BROUILLON' ? 'Brouillon' : status === 'PUBLIEE' ? 'Publiée' : 'Archivée';
    }

    private fetchAssociations(page: number) {
        return this.associations.fetchAssociations({ page, size: 50 });
    }

    private loadPresidentAssociations(associations: Association[]): void {
        this.presidentAssociations.set([]);
        this.presidentLookupError.set(false);
        if (!this.authenticated() || associations.length === 0) {
            this.finishManagerSetup();
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
                this.finishManagerSetup();
            }
        });
    }

    private finishManagerSetup(): void {
        this.canManageNews.set(this.campusAdmin() || this.presidentAssociations().length > 0);
        this.loadManagement(1);
    }

    private emptyNews(): CreateNewsRequest {
        return { title: '', content: '', category: '', associationId: null };
    }

    private handleNewsError(error: { status: number }): void {
        this.savingNews.set(false);
        this.newsError.set(error.status === 401 || error.status === 403
            ? 'Tu n’as pas les droits nécessaires pour gérer cette actualité.'
            : 'Impossible d’enregistrer l’actualité. Vérifie les informations ou réessaie.');
    }
}
