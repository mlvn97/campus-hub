import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { Observable, Subject, tap } from "rxjs";
import { ProfileResponse } from "../../dto/profile/profile-response.dto";
import { ProfileUpdateRequest } from "../../dto/profile/profile-update-request.dto";

@Injectable({
    providedIn: 'root'
})
export class ProfileService {
    private readonly profileChanges = new Subject<void>();
    readonly profileChanged = this.profileChanges.asObservable();

    private url: string;

    constructor(private http: HttpClient, private api: ApiService) {
        this.url = `${this.api.userUrl}/profiles`;
    }

    fetchMyProfile(): Observable<ProfileResponse> {
        return this.http.get<ProfileResponse>(`${this.url}/me`);
    }

    fetchProfile(userId: number): Observable<ProfileResponse> {
        return this.http.get<ProfileResponse>(`${this.url}/${userId}`);
    }

    updateMyProfile(data: ProfileUpdateRequest): Observable<ProfileResponse> {
        return this.http.patch<ProfileResponse>(
            `${this.url}/me`,
            data
        ).pipe(tap(() => this.profileChanges.next()));
    }
}