import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { LoginRequest } from "../../dto/user/login-request.dto";
import { Observable, tap } from "rxjs";
import { LoginResponse } from "../../dto/user/login-response.dto";
import { response } from "express";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    constructor(private http: HttpClient, private api: ApiService) {}

    login(data: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(
            `${this.api.userUrl}/login`,
            data
        )
        .pipe(tap(response => {
            localStorage.setItem('token', response.token);
        }));
    }

    logout(): void {
        localStorage.removeItem('token');
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }
}