import { afterNextRender, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { catchError, map, merge, of, startWith, switchMap } from 'rxjs';

import { AuthService } from './core/services/auth.service';
import { ProfileService } from './core/services/profile.service';
import { UserService } from './core/services/user.service';

@Component({
    imports: [RouterLink, RouterLinkActive, RouterOutlet],
    selector: 'app-root',
    styleUrl: './app.css',
    templateUrl: './app.html'
})
export class App {
    private readonly auth = inject(AuthService);
    private readonly profiles = inject(ProfileService);
    private readonly users = inject(UserService);
    private readonly destroyRef = inject(DestroyRef);
    protected readonly account = signal<{ name: string; photo: string; initials: string } | null>(null);
    protected readonly photoFailed = signal(false);

    constructor() {
        afterNextRender(() => {
            merge(this.auth.sessionChanged, this.profiles.profileChanged).pipe(
                startWith(undefined),
                switchMap(() => {
                    if (!this.auth.isAuthenticated()) {
                        return of(null);
                    }

                    return this.profiles.fetchMyProfile().pipe(
                        switchMap(profile => this.users.fetchUser(profile.userId).pipe(
                            map(user => {
                                const url = profile.profilePictureUrl ?? '';
                                return {
                                    name: user.name,
                                    photo: profile.profilePictureData || (/^https?:\/\//i.test(url) ? url : ''),
                                    initials: user.name.trim().split(/\s+/).slice(0, 2)
                                        .map(part => part.charAt(0).toUpperCase()).join('')
                                };
                            })
                        )),
                        catchError(() => of(null))
                    );
                }),
                takeUntilDestroyed(this.destroyRef)
            ).subscribe(account => {
                this.photoFailed.set(false);
                this.account.set(account);
            });
        });
    }
}
