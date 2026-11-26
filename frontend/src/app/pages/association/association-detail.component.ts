import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { catchError, distinctUntilChanged, forkJoin, map, of, Subscription } from 'rxjs';

import { AssociationService } from '../../core/services/association.service';
import { AuthService } from '../../core/services/auth.service';
import { MembershipService } from '../../core/services/membership.service';
import { SubscriptionService } from '../../core/services/subscription.service';
import { UserService } from '../../core/services/user.service';
import { Association, AssociationCategory } from '../../models/association.model';
import { Membership } from '../../models/membership.model';

@Component({
    selector: 'app-association-detail',
    imports: [FormsModule, RouterLink],
    templateUrl: './association-detail.component.html',
    styleUrl: './association-detail.component.css'
})
export class AssociationDetailComponent {
    private readonly route = inject(ActivatedRoute);
    private readonly associationService = inject(AssociationService);
    private readonly auth = inject(AuthService);
    private readonly memberships = inject(MembershipService);
    private readonly subscriptions = inject(SubscriptionService);
    private readonly users = inject(UserService);
    private readonly destroyRef = inject(DestroyRef);
    private detailRequest?: Subscription;
    private membershipRequest?: Subscription;
    private membershipActionRequest?: Subscription;
    private leaveMembersRequest?: Subscription;
    private successorNamesRequest?: Subscription;
    private currentId: string | null = null;

    protected readonly item = signal<Association | null>(null);
    protected readonly loading = signal(true);
    protected readonly error = signal<number | null>(null);
    protected readonly authenticated = signal(false);
    protected readonly canRequestMembership = signal(false);
    protected readonly membership = signal<Membership | null>(null);
    protected readonly membershipLoading = signal(false);
    protected readonly membershipError = signal('');
    protected readonly membershipNeedsLogin = signal(false);
    protected readonly membershipActionMessage = signal('');
    protected readonly membershipActionError = signal('');
    protected readonly membershipActionNeedsLogin = signal(false);
    protected readonly membershipActionLoading = signal(false);
    protected readonly requestSent = signal(false);
    protected readonly leaveDialogLoading = signal(false);
    protected readonly leaveError = signal('');
    protected readonly leaveSuccessors = signal<Membership[]>([]);
    protected readonly successorNames = signal<Record<number, string>>({});
    protected readonly successorLookupFailed = signal(false);
    protected successorUserId: number | null = null;
    protected joinMotivation = '';

    private readonly categories: Record<AssociationCategory, string> = {
        CULTURE: 'Culture',
        SPORT: 'Sport',
        TECH: 'Technologie',
        HUMANITARIAN: 'Humanitaire',
        OTHER: 'Autre'
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

    protected openJoinDialog(dialog: HTMLDialogElement): void {
        if (!this.canRequestMembership() || this.item()?.status !== 'ACTIVE' || this.requestSent()) {
            return;
        }
        this.joinMotivation = '';
        this.membershipActionError.set('');
        this.membershipActionNeedsLogin.set(false);
        dialog.showModal();
    }

    protected requestMembership(dialog: HTMLDialogElement): void {
        const association = this.item();
        const motivation = this.joinMotivation.trim();
        if (!association || !this.canRequestMembership() || association.status !== 'ACTIVE'
            || this.membershipActionLoading() || !motivation) {
            return;
        }

        this.membershipActionLoading.set(true);
        this.membershipActionError.set('');
        this.membershipActionNeedsLogin.set(false);
        this.membershipActionRequest = this.subscriptions.submitSubscriptionFor(association.id, { motivation })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.membershipActionLoading.set(false);
                    this.requestSent.set(true);
                    this.joinMotivation = '';
                    dialog.close();
                    this.membershipActionMessage.set('Ta demande d’adhésion a été envoyée.');
                },
                error: (error: HttpErrorResponse) => {
                    this.membershipActionLoading.set(false);
                    this.membershipActionNeedsLogin.set(error.status === 401);
                    this.membershipActionError.set(error.status === 401
                        ? 'Connecte-toi pour envoyer une demande d’adhésion.'
                        : error.status === 403
                            ? 'Tu n’as pas le droit d’envoyer cette demande.'
                            : error.status === 409
                                ? 'Tu as déjà envoyé une demande ou tu es déjà membre de cette association.'
                                : 'Impossible d’envoyer ta demande. Vérifie les informations ou réessaie.');
                }
            });
    }

    protected openLeaveDialog(dialog: HTMLDialogElement): void {
        const association = this.item();
        const membership = this.membership();
        if (!association || !membership || this.membershipActionLoading()) {
            return;
        }

        this.leaveError.set('');
        this.membershipActionError.set('');
        this.membershipActionNeedsLogin.set(false);
        this.leaveSuccessors.set([]);
        this.successorNames.set({});
        this.successorLookupFailed.set(false);
        this.successorUserId = null;
        this.leaveMembersRequest?.unsubscribe();
        this.successorNamesRequest?.unsubscribe();
        dialog.showModal();

        if (membership.role !== 'PRESIDENT') {
            return;
        }

        this.leaveDialogLoading.set(true);
        this.leaveMembersRequest = this.memberships.fetchMembers(association.id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: members => {
                    const successors = members.filter(member => member.userId !== membership.userId);
                    this.leaveSuccessors.set(successors);
                    this.leaveDialogLoading.set(false);
                    if (successors.length === 0) {
                        this.leaveError.set('Il faut un autre membre pour prendre la présidence avant de quitter.');
                        return;
                    }

                    this.successorNamesRequest = forkJoin(successors.map(member =>
                        this.users.fetchUser(member.userId).pipe(
                            map(user => ({ userId: member.userId, name: user.name, lookupFailed: false })),
                            catchError(() => of({
                                userId: member.userId,
                                name: `Membre #${member.userId}`,
                                lookupFailed: true
                            }))
                        )
                    )).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(names => {
                        this.successorNames.set(Object.fromEntries(
                            names.map(({ userId, name }) => [userId, name])
                        ));
                        this.successorLookupFailed.set(names.some(result => result.lookupFailed));
                    });
                },
                error: () => {
                    this.leaveDialogLoading.set(false);
                    this.leaveError.set('Impossible de charger les membres pour choisir un successeur.');
                }
            });
    }

    protected confirmLeave(dialog: HTMLDialogElement): void {
        const association = this.item();
        const membership = this.membership();
        if (!association || !membership || this.membershipActionLoading()) {
            return;
        }
        if (membership.role === 'PRESIDENT' && this.successorUserId === null) {
            this.leaveError.set('Choisis un membre qui reprendra la présidence.');
            return;
        }

        this.membershipActionLoading.set(true);
        this.membershipActionError.set('');
        this.membershipActionRequest = this.memberships.leave(
            association.id, membership.userId, this.successorUserId ?? undefined
        )
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.membershipActionLoading.set(false);
                    this.membership.set(null);
                    dialog.close();
                    this.membershipActionMessage.set('Tu as quitté cette association.');
                },
                error: (error: HttpErrorResponse) => {
                    this.membershipActionLoading.set(false);
                    const message = error.status === 409
                        ? 'La présidence doit être transmise à un autre membre avant de quitter.'
                        : 'Impossible de quitter cette association. Réessaie.';
                    this.leaveError.set(message);
                    this.membershipActionError.set(message);
                }
            });
    }

    protected categoryLabel(category: AssociationCategory): string {
        return this.categories[category];
    }

    private load(rawId: string | null): void {
        this.currentId = rawId;
        this.detailRequest?.unsubscribe();
        this.membershipRequest?.unsubscribe();
        this.membershipActionRequest?.unsubscribe();
        this.leaveMembersRequest?.unsubscribe();
        this.successorNamesRequest?.unsubscribe();
        this.item.set(null);
        this.membership.set(null);
        this.error.set(null);
        this.membershipError.set('');
        this.membershipNeedsLogin.set(false);
        this.membershipActionMessage.set('');
        this.membershipActionError.set('');
        this.membershipActionNeedsLogin.set(false);
        this.membershipActionLoading.set(false);
        this.requestSent.set(false);
        this.leaveDialogLoading.set(false);
        this.leaveError.set('');
        this.authenticated.set(this.auth.isAuthenticated());
        this.canRequestMembership.set(this.authenticated()
            && !this.auth.adminAssociations() && !this.auth.adminCampus());

        const id = Number(rawId);
        if (!rawId || !Number.isSafeInteger(id) || id < 1) {
            this.loading.set(false);
            this.error.set(404);
            return;
        }

        this.loading.set(true);
        this.detailRequest = this.associationService.fetchAssociation(id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: item => {
                    this.item.set(item);
                    this.loading.set(false);
                    this.loadMembership(item.id);
                },
                error: (error: HttpErrorResponse) => {
                    this.loading.set(false);
                    this.error.set(error.status);
                }
            });
    }

    private loadMembership(associationId: number): void {
        if (!this.authenticated()) {
            this.membershipLoading.set(false);
            return;
        }

        this.membershipLoading.set(true);
        this.membershipRequest = this.memberships.fetchMyMembership(associationId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: membership => {
                    this.membership.set(membership);
                    this.membershipLoading.set(false);
                },
                error: (error: HttpErrorResponse) => {
                    this.membershipLoading.set(false);
                    this.membershipNeedsLogin.set(error.status === 401);
                    this.membershipError.set(error.status === 401
                        ? 'Ta session a expiré. Reconnecte-toi pour gérer ton adhésion.'
                        : 'Impossible de vérifier ton adhésion. Réessaie plus tard.');
                }
            });
    }
}
