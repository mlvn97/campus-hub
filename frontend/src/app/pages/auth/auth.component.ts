import { CommonModule } from "@angular/common";
import { Component, inject } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { UserService } from "../../core/services/user.service";
import { AuthService } from "../../core/services/auth.service";
import { ActivatedRoute, Router } from "@angular/router";

@Component({
    selector: 'app-auth',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './auth.component.html',
    styleUrl: './auth.component.css'
})
export class AuthComponent {

    private formbuilder = inject(FormBuilder);
    private userService = inject(UserService);
    private authService = inject(AuthService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);

    isLogin = true;
    errorMessage = '';
    successMessage = '';

    registerForm = this.formbuilder.group({
        firstName: ['', [Validators.required, Validators.maxLength(50)]],
        lastName: ['', [Validators.required, Validators.maxLength(50)]],
        email: ['', [Validators.required, Validators.email, Validators.maxLength(50)]],
        password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(50)]]
    });

    loginForm = this.formbuilder.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required]]
    });

    switchMode(): void {
        this.isLogin = !this.isLogin;
        this.errorMessage = '';
        this.successMessage = '';
    }

    login(): void {
        if (this.loginForm.invalid) {
            this.loginForm.markAllAsTouched();
            return;
        }

        this.errorMessage = '';
        this.authService.login(this.loginForm.getRawValue()).subscribe({
            next: () => {
                const destination = this.route.snapshot.queryParamMap.get('destination');
                this.router.navigate([destination === 'compte' ? '/compte' : '/']);
            },
            error: () => {
                this.errorMessage = 'Email ou mot de passe incorrect.';
            }
        });
    }

    register(): void {
        if (this.registerForm.invalid) {
            this.registerForm.markAllAsTouched();
            return;
        }

        this.errorMessage = '';
        const { firstName, lastName, email, password } = this.registerForm.getRawValue();
        this.userService.register({
            name: `${firstName} ${lastName}`.trim(),
            email,
            password
        }).subscribe({
            next: () => {
                this.successMessage = 'Compte créé avec succès. Vous pouvez vous connecter.';
                this.isLogin = true;
                this.registerForm.reset();
            },
            error: () => {
                this.errorMessage = 'Une erreur est survenue lors de la création du compte.';
            }
        });
    }
}