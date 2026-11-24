import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { LoginRequest } from "../../dto/user/login-request.dto";
import { Observable, Subject, tap } from "rxjs";
import { LoginResponse } from "../../dto/user/login-response.dto";


@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly sessionChanges = new Subject<void>();
    readonly sessionChanged = this.sessionChanges.asObservable();

    constructor(private http: HttpClient, private api: ApiService) {}

    login(data: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(
            `${this.api.userUrl}/login`,
            data
        )
        .pipe(tap(response => {
            localStorage.setItem('token', response.token);
            localStorage.setItem('role', response.user.role);
            console.log("rôle: " + response.user.role);
            this.sessionChanges.next();
        }));
    }

    logout(): void {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        this.sessionChanges.next();
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    getRole(): string | null {
        return localStorage.getItem('role');
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    adminCampus(): boolean {
        return this.getRole() === "ADMIN_CAMPUS";
    }

    adminAssociations(): boolean {
        return this.getRole() === "ADMIN_ASSOCIATION";
    }
}