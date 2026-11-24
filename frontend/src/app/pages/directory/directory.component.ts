import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, from, map, mergeMap, of, switchMap, toArray } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { AssociationFilters, AssociationService } from '../../core/services/association.service';
import { MembershipService } from '../../core/services/membership.service';
import { SubscriptionService } from '../../core/services/subscription.service';
import { UserService } from '../../core/services/user.service';
import { AssociationRequest } from '../../dto/association/association-request.dto';
import { SubscriptionRequest } from '../../dto/association/subscription/membership-subscription-request.dto';
import { MemberRole } from '../../enums/associations/membership-role.type';
import { Association, AssociationCategory, AssociationStatus } from '../../models/association.model';
import { Membership } from '../../models/membership.model';
import { Subscription } from '../../models/subscription.model';

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
    private readonly auth = inject(AuthService);
    private readonly memberships = inject(MembershipService);
    private readonly subscriptions = inject(SubscriptionService);
    private readonly users = inject(UserService);
    private readonly destroyRef = inject(DestroyRef);
    private filters: AssociationFilters = {};
    protected readonly items = signal<Association[]>([]);
    protected readonly page = signal(0);
    protected readonly totalPages = signal(0);
    protected readonly loading = signal(true);
    protected readonly error = signal<number | null>(null);
    protected readonly preview = signal(false);
    protected readonly canCreate = signal(false);
    protected readonly canManage = signal(false);
    protected readonly canRequestMembership = signal(false);
    protected readonly creating = signal(false);
    protected readonly createError = signal('');
    protected readonly createMessage = signal('');
    protected readonly joinMessage = signal<{ text: string; error: boolean } | null>(null);
    protected readonly joining = signal(false);
    protected readonly membershipsByAssociation = signal<Record<number, Membership | null>>({});
    protected readonly membershipLookupFailed = signal(false);
    protected readonly leavingAssociationId = signal<number | null>(null);
    protected readonly leaveAssociationSelection = signal<Association | null>(null);
    protected readonly leaveMembership = signal<Membership | null>(null);
    protected readonly leaveSuccessors = signal<Membership[]>([]);
    protected readonly leaveLoading = signal(false);
    protected readonly leaveError = signal('');
    protected successorUserId: number | null = null;
    protected readonly selectedAssociation = signal<Association | null>(null);
    protected readonly confirmation = signal<{ message: string; action: () => void } | null>(null);
    protected joinMotivation = '';
    protected readonly members = signal<Membership[]>([]);
    protected readonly userNames = signal<Record<number, string>>({});
    protected readonly userNameLookupFailed = signal(false);
    protected readonly memberRoleOptions: MemberRole[] = ['PRESIDENT', 'TREASURER', 'MEMBER'];
    protected memberRoles: Record<number, MemberRole> = {};
    protected readonly applications = signal<Subscription[]>([]);
    protected readonly manageLoading = signal(false);
    protected readonly manageError = signal('');
    protected readonly manageMessage = signal('');
    protected readonly managingSubscriptionId = signal<number | null>(null);
    protected readonly managingMemberId = signal<number | null>(null);
    protected readonly suspending = signal(false);
    protected readonly reactivating = signal(false);
    protected readonly categories: Record<AssociationCategory, string> = {
        CULTURE: 'Culture', SPORT: 'Sport', TECH: 'Technologie',
        HUMANITARIAN: 'Humanitaire', OTHER: 'Autre'
    };
    protected readonly categoryIcons: Record<AssociationCategory, string> = {
        CULTURE: 'bi bi-music-note-beamed',
        SPORT: 'bi bi-trophy',
        TECH: 'bi bi-code-slash',
        HUMANITARIAN: 'bi bi-heart',
        OTHER: 'bi bi-people'
    };
    protected readonly categoryKeys = Object.keys(this.categories) as AssociationCategory[];
    protected category: AssociationCategory | '' = '';
    protected status: AssociationStatus | '' = '';
    protected newAssociation: AssociationRequest = this.emptyAssociation();

    constructor() {
        afterNextRender(() => {
            this.canCreate.set(this.auth.adminAssociations() || this.auth.adminCampus());
            this.canManage.set(this.auth.adminAssociations() || this.auth.adminCampus());
            this.canRequestMembership.set(!this.auth.adminAssociations() && !this.auth.adminCampus());
            this.load(0);
        });
    }

    protected applyFilters(): void {
        this.filters = { category: this.category || undefined, status: this.status || undefined };
        this.createMessage.set('');
        this.joinMessage.set(null);
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
                    this.loadMyMemberships(result.content);
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

    protected openCreateDialog(dialog: HTMLDialogElement): void {
        this.newAssociation = this.emptyAssociation();
        this.createError.set('');
        this.createMessage.set('');
        dialog.showModal();
    }

    protected openJoinDialog(association: Association, dialog: HTMLDialogElement): void {
        if (!this.canRequestMembership() || association.status === 'SUSPENDED') {
            return;
        }
        this.selectedAssociation.set(association);
        this.joinMotivation = '';
        this.joinMessage.set(null);
        dialog.showModal();
    }

    protected leaveAssociation(
        association: Association,
        dialog: HTMLDialogElement,
        confirmationDialog: HTMLDialogElement
    ): void {
        const membership = this.membershipsByAssociation()[association.id];
        if (!membership || this.leavingAssociationId() !== null) {
            return;
        }

        if (membership.role === 'PRESIDENT') {
            this.leaveAssociationSelection.set(association);
            this.leaveMembership.set(membership);
            this.leaveSuccessors.set([]);
            this.leaveLoading.set(true);
            this.successorUserId = null;
            this.leaveError.set('');
            dialog.showModal();
            this.memberships.fetchMembers(association.id)
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({
                    next: members => {
                        this.leaveLoading.set(false);
                        const successors = members.filter(member => member.userId !== membership.userId);
                        this.leaveSuccessors.set(successors);
                        if (successors.length === 0) {
                            this.leaveError.set('Il faut un autre membre pour prendre la présidence avant de quitter.');
                            return;
                        }
                        this.fetchUserNames(successors.map(member => member.userId))
                            .pipe(takeUntilDestroyed(this.destroyRef))
                            .subscribe(names => this.userNames.update(current => ({
                                ...current,
                                ...Object.fromEntries(names.map(user => [user.userId, user.name]))
                            })));
                    },
                    error: () => {
                        this.leaveLoading.set(false);
                        this.leaveError.set('Impossible de charger les membres pour choisir un successeur.');
                    }
                });
            return;
        }

        this.openConfirmation(
            `Quitter l’association « ${association.name} » ?`,
            () => this.performLeave(association, membership),
            confirmationDialog
        );
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

    protected confirmLeaveAssociation(dialog: HTMLDialogElement): void {
        const association = this.leaveAssociationSelection();
        const membership = this.leaveMembership();
        if (!association || !membership || this.leavingAssociationId() !== null) {
            return;
        }
        if (membership.role === 'PRESIDENT' && this.successorUserId === null) {
            this.leaveError.set('Choisis un membre qui reprendra la présidence.');
            return;
        }
        this.performLeave(association, membership, this.successorUserId ?? undefined, dialog);
    }

    protected submitJoinRequest(dialog: HTMLDialogElement): void {
        const association = this.selectedAssociation();
        const motivation = this.joinMotivation.trim();
        if (!association || this.joining() || !motivation) {
            return;
        }

        this.joining.set(true);
        this.joinMessage.set(null);
        const request: SubscriptionRequest = { motivation };
        this.subscriptions.submitSubscriptionFor(association.id, request)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.joining.set(false);
                    dialog.close();
                    this.joinMessage.set({
                        text: "Votre demande d'inscription a été prise en compte.",
                        error: false
                    });
                },
                error: (error: HttpErrorResponse) => {
                    this.joining.set(false);
                    this.joinMessage.set({ text: this.joinErrorMessage(error), error: true });
                }
            });
    }

    protected openManageDialog(association: Association, dialog: HTMLDialogElement): void {
        this.selectedAssociation.set(association);
        this.members.set([]);
        this.userNames.set({});
        this.userNameLookupFailed.set(false);
        this.memberRoles = {};
        this.applications.set([]);
        this.manageError.set('');
        this.manageMessage.set('');
        this.manageLoading.set(true);
        dialog.showModal();
        this.loadManagementData(association.id);
    }

    protected manageApplication(application: Subscription, approve: boolean): void {
        const association = this.selectedAssociation();
        if (!association || this.managingSubscriptionId() !== null) {
            return;
        }

        this.managingSubscriptionId.set(application.id);
        this.manageError.set('');
        this.manageMessage.set('');
        const action = approve
            ? this.subscriptions.approveSubscription(association.id, application.id)
            : this.subscriptions.rejectSubscription(association.id, application.id);
        action.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: () => {
                this.managingSubscriptionId.set(null);
                this.manageMessage.set(approve ? 'La demande a été acceptée.' : 'La demande a été refusée.');
                this.loadManagementData(association.id);
            },
            error: () => {
                this.managingSubscriptionId.set(null);
                this.manageError.set('Impossible de traiter cette demande. Vérifie tes droits ou réessaie.');
            }
        });
    }

    protected changeMemberRole(member: Membership): void {
        const association = this.selectedAssociation();
        const role = this.memberRoles[member.userId];
        if (!association || !role || role === member.role || this.managingMemberId() !== null) {
            return;
        }

        this.managingMemberId.set(member.userId);
        this.manageError.set('');
        this.manageMessage.set('');
        this.memberships.changeMemberRole(association.id, member.userId, role)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.managingMemberId.set(null);
                    this.manageMessage.set('Modification effectuée.');
                    this.loadManagementData(association.id);
                },
                error: () => {
                    this.managingMemberId.set(null);
                    this.manageError.set('Échec de la modification.');
                }
            });
    }

    protected excludeMember(member: Membership, dialog: HTMLDialogElement): void {
        const association = this.selectedAssociation();
        if (!association || this.managingMemberId() !== null) {
            return;
        }

        this.openConfirmation(
            `Exclure ${this.userName(member.userId)} de l’association « ${association.name} » ?`,
            () => this.performExcludeMember(association, member),
            dialog
        );
    }

    protected suspendAssociation(dialog: HTMLDialogElement): void {
        const association = this.selectedAssociation();
        if (!association || association.status !== 'ACTIVE' || this.suspending()) {
            return;
        }
        this.openConfirmation(
            `Suspendre l’association « ${association.name} » ?`,
            () => this.performSuspendAssociation(association),
            dialog
        );
    }

    protected reactivateAssociation(dialog: HTMLDialogElement): void {
        const association = this.selectedAssociation();
        if (!association || association.status !== 'SUSPENDED' || this.reactivating()) {
            return;
        }
        this.openConfirmation(
            `Rétablir l’association « ${association.name} » ?`,
            () => this.performReactivateAssociation(association),
            dialog
        );
    }

    private openConfirmation(message: string, action: () => void, dialog: HTMLDialogElement): void {
        this.confirmation.set({ message, action });
        dialog.showModal();
    }

    private performExcludeMember(association: Association, member: Membership): void {
        this.managingMemberId.set(member.userId);
        this.manageError.set('');
        this.manageMessage.set('');
        this.memberships.removeMember(association.id, member.userId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.managingMemberId.set(null);
                    this.manageMessage.set('Modification effectuée.');
                    this.loadManagementData(association.id);
                },
                error: () => {
                    this.managingMemberId.set(null);
                    this.manageError.set('Échec de la modification.');
                }
            });
    }

    private performSuspendAssociation(association: Association): void {
        this.suspending.set(true);
        this.manageError.set('');
        this.associations.suspendAssociation(association.id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: updated => {
                    this.suspending.set(false);
                    this.selectedAssociation.set(updated);
                    this.items.update(items => items.map(item => item.id === updated.id ? updated : item));
                    this.manageMessage.set('L’association a été suspendue.');
                },
                error: () => {
                    this.suspending.set(false);
                    this.manageError.set('Impossible de suspendre l’association. Vérifie tes droits ou réessaie.');
                }
            });
    }

    private performReactivateAssociation(association: Association): void {
        this.reactivating.set(true);
        this.manageError.set('');
        this.associations.reactivateAssociation(association.id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: updated => {
                    this.reactivating.set(false);
                    this.selectedAssociation.set(updated);
                    this.items.update(items => items.map(item => item.id === updated.id ? updated : item));
                    this.manageMessage.set('L’association a été rétablie.');
                },
                error: () => {
                    this.reactivating.set(false);
                    this.manageError.set('Impossible de rétablir l’association. Vérifie tes droits ou réessaie.');
                }
            });
    }

    protected formatJoinedAt(value: string): string {
        return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' }).format(new Date(value));
    }

    protected userName(userId: number): string {
        return this.userNames()[userId] ?? `Membre #${userId}`;
    }

    private loadMyMemberships(associations: Association[]): void {
        this.membershipsByAssociation.set({});
        this.membershipLookupFailed.set(false);
        if (!this.auth.isAuthenticated() || associations.length === 0) {
            this.loading.set(false);
            return;
        }

        forkJoin(associations.map(association => this.memberships.fetchMyMembership(association.id).pipe(
            catchError(() => {
                this.membershipLookupFailed.set(true);
                return of(null);
            })
        ))).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: memberships => {
                this.membershipsByAssociation.set(Object.fromEntries(
                    associations.map((association, index) => [association.id, memberships[index]])
                ));
                this.loading.set(false);
            },
            error: () => {
                this.membershipLookupFailed.set(true);
                this.loading.set(false);
            }
        });
    }

    private performLeave(
        association: Association,
        membership: Membership,
        successorId?: number,
        dialog?: HTMLDialogElement
    ): void {
        this.leavingAssociationId.set(association.id);
        this.leaveError.set('');
        this.joinMessage.set(null);
        this.memberships.leave(association.id, membership.userId, successorId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.leavingAssociationId.set(null);
                    this.membershipsByAssociation.update(current => ({ ...current, [association.id]: null }));
                    dialog?.close();
                    this.joinMessage.set({ text: 'Tu as quitté cette association.', error: false });
                },
                error: (error: HttpErrorResponse) => {
                    this.leavingAssociationId.set(null);
                    const message = error.status === 409
                        ? 'La présidence doit être transmise à un autre membre avant de quitter.'
                        : 'Impossible de quitter cette association.';
                    this.leaveError.set(message);
                    this.joinMessage.set({ text: message, error: true });
                }
            });
    }

    protected createAssociation(form: NgForm, dialog: HTMLDialogElement): void {
        if (!this.canCreate() || this.creating() || form.invalid) {
            return;
        }

        this.creating.set(true);
        this.createError.set('');
        this.associations.createAssociation(this.newAssociation)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: association => {
                    this.creating.set(false);
                    dialog.close();
                    this.newAssociation = this.emptyAssociation();
                    form.resetForm(this.newAssociation);
                    this.createMessage.set(`L’association « ${association.name} » a été créée.`);
                    this.load(0);
                },
                error: (error: HttpErrorResponse) => {
                    this.creating.set(false);
                    this.createError.set(error.status === 401 || error.status === 403
                        ? 'Tu n’as pas les droits nécessaires pour créer une association.'
                        : 'Impossible de créer l’association. Vérifie les informations ou réessaie.');
                }
            });
    }

    private emptyAssociation(): AssociationRequest {
        return { name: '', description: '', category: 'OTHER', status: 'ACTIVE' };
    }

    private loadManagementData(associationId: number): void {
        this.manageLoading.set(true);
        this.userNameLookupFailed.set(false);
        forkJoin({
            members: this.memberships.fetchMembers(associationId),
            applications: this.subscriptions.fetchSubscriptionsOf(associationId)
        }).pipe(
            switchMap(result => {
                const applications = result.applications.filter(application => application.status === 'PENDING');
                const userIds = [...new Set([
                    ...result.members.map(member => member.userId),
                    ...applications.map(application => application.userId)
                ])];
                return this.fetchUserNames(userIds).pipe(
                    map(userNames => ({ ...result, applications, userNames }))
                );
            }),
            takeUntilDestroyed(this.destroyRef)
        ).subscribe({
            next: result => {
                this.members.set(result.members);
                this.memberRoles = Object.fromEntries(
                    result.members.map(member => [member.userId, member.role])
                );
                this.applications.set(result.applications);
                this.userNames.set(Object.fromEntries(
                    result.userNames.map(user => [user.userId, user.name])
                ));
                this.manageLoading.set(false);
                this.manageError.set('');
            },
            error: () => {
                this.manageLoading.set(false);
                this.manageError.set('Impossible de charger les membres et les demandes. Vérifie tes droits ou réessaie.');
            }
        });
    }

    private fetchUserNames(userIds: number[]) {
        if (userIds.length === 0) {
            return of([] as { userId: number; name: string }[]);
        }

        return from(userIds).pipe(
            mergeMap(userId => this.users.fetchUser(userId).pipe(
                map(user => ({ userId, name: user.name })),
                catchError(() => {
                    this.userNameLookupFailed.set(true);
                    return of({ userId, name: `Membre #${userId}` });
                })
            ), 6),
            toArray()
        );
    }

    private joinErrorMessage(error: HttpErrorResponse): string {
        if (error.status === 401) {
            return 'Connecte-toi pour envoyer une demande d’inscription.';
        }
        if (error.status === 403) {
            return 'Tu n’as pas le droit d’envoyer cette demande.';
        }
        if (error.status === 409) {
            return 'Tu as déjà envoyé une demande ou tu es déjà membre de cette association.';
        }
        return 'Impossible d’envoyer ta demande. Vérifie les informations ou réessaie.';
    }
}
