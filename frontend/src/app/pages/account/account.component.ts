import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { map, switchMap } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';
import { UserService } from '../../core/services/user.service';
import { ProfileResponse, StudentGender } from '../../dto/user/profile/profile-response.dto';
import { UserResponse } from '../../dto/user/register-response.dto';

@Component({
    selector: 'app-account',
    imports: [FormsModule, RouterLink],
    templateUrl: './account.component.html',
    styleUrl: './account.component.css'
})
export class AccountComponent {
    private readonly auth = inject(AuthService);
    private readonly profiles = inject(ProfileService);
    private readonly users = inject(UserService);
    private readonly router = inject(Router);
    private readonly destroyRef = inject(DestroyRef);

    protected readonly user = signal<UserResponse | null>(null);
    protected readonly photo = signal('');
    protected readonly loading = signal(true);
    protected readonly saving = signal(false);
    protected readonly loadError = signal(false);
    protected readonly message = signal('');
    protected readonly saveError = signal('');
    protected bio = '';
    protected studentGender: StudentGender = 'NON_RENSEIGNE';
    protected readonly preparingPhoto = signal(false);
    protected readonly photoError = signal('');
    private pendingPhoto?: string;

    constructor() {
        afterNextRender(() => this.load());
    }

    protected load(): void {
        if (!this.auth.isAuthenticated()) {
            this.loading.set(false);
            return;
        }

        this.loading.set(true);
        this.loadError.set(false);
        this.profiles.fetchMyProfile().pipe(
            switchMap(profile => this.users.fetchUser(profile.userId).pipe(map(user => ({ profile, user })))),
            takeUntilDestroyed(this.destroyRef)
        ).subscribe({
            next: ({ profile, user }) => {
                this.setProfile(profile);
                this.user.set(user);
                this.loading.set(false);
            },
            error: (error: HttpErrorResponse) => {
                this.loading.set(false);
                if (error.status === 401 || error.status === 403) {
                    this.auth.logout();
                } else {
                    this.loadError.set(true);
                }
            }
        });
    }

    protected save(form: NgForm): void {
        if (form.invalid || this.saving() || this.preparingPhoto()) {
            return;
        }

        this.message.set('');
        this.saveError.set('');
        this.saving.set(true);
        this.profiles.updateMyProfile({
            bio: this.bio,
            profilePictureData: this.pendingPhoto,
            studentGender: this.user()?.role === 'STUDENT' ? this.studentGender : undefined
        })
            .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
                next: profile => {
                    this.setProfile(profile);
                    this.saving.set(false);
                    this.message.set('Profil mis à jour.');
                },
                error: (error: HttpErrorResponse) => {
                    this.saving.set(false);
                    if (error.status === 401 || error.status === 403) {
                        this.logout();
                    } else {
                        this.saveError.set('Impossible d’enregistrer les modifications. Vérifie les champs ou réessaie.');
                    }
                }
            });
    }

    protected async choosePhoto(input: HTMLInputElement): Promise<void> {
        const file = input.files?.[0];
        input.value = '';
        if (!file) {
            return;
        }

        this.photoError.set('');
        if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || file.size > 10 * 1024 * 1024) {
            this.photoError.set('Choisis une image JPEG, PNG ou WebP de 10 Mo maximum.');
            return;
        }

        this.preparingPhoto.set(true);
        try {
            const bitmap = await createImageBitmap(file);
            try {
                const canvas = document.createElement('canvas');
                const scale = Math.min(1, 512 / Math.max(bitmap.width, bitmap.height));
                canvas.width = Math.max(1, Math.round(bitmap.width * scale));
                canvas.height = Math.max(1, Math.round(bitmap.height * scale));
                const context = canvas.getContext('2d');
                if (!context) {
                    throw new Error('Canvas unavailable');
                }
                context.fillStyle = '#fffdfa';
                context.fillRect(0, 0, canvas.width, canvas.height);
                context.drawImage(bitmap, 0, 0, canvas.width, canvas.height);
                const data = canvas.toDataURL('image/jpeg', 0.85);
                if (!data.startsWith('data:image/jpeg;base64,') || data.length > 350_000) {
                    throw new Error('Invalid image output');
                }
                this.pendingPhoto = data;
                this.photo.set(data);
            } finally {
                bitmap.close();
            }
        } catch {
            this.photoError.set('Cette image ne peut pas être chargée.');
        } finally {
            this.preparingPhoto.set(false);
        }
    }

    protected logout(): void {
        this.auth.logout();
        void this.router.navigate(['/auth']);
    }

    protected initials(name: string): string {
        return name.trim().split(/\s+/).slice(0, 2).map(part => part.charAt(0).toUpperCase()).join('');
    }

    protected roleLabel(role: string): string {
        if (role === 'STUDENT') {
            return { MASCULIN: 'Étudiant', FEMININ: 'Étudiante', NON_RENSEIGNE: 'Étudiant·e' }[this.studentGender];
        }
        const labels: Record<string, string> = {
            ADMIN_ASSOCIATION: 'Administration d’association',
            ADMIN_CAMPUS: 'Administration du campus'
        };
        return labels[role] ?? role;
    }

    private setProfile(profile: ProfileResponse): void {
        this.bio = profile.bio ?? '';
        this.studentGender = profile.studentGender ?? 'NON_RENSEIGNE';
        this.pendingPhoto = undefined;
        const previousUrl = profile.profilePictureUrl ?? '';
        this.photo.set(profile.profilePictureData || (/^https?:\/\//i.test(previousUrl) ? previousUrl : ''));
    }
}
