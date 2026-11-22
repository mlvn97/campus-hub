import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { Observable } from "rxjs";
import { UserResponse } from "../../dto/user/register-response.dto";
import { UserRegistrationRequest } from "../../dto/user/register-request.dto";

@Injectable({
    providedIn: 'root'
})
export class UserService {

    constructor(private http: HttpClient, private api: ApiService) {}

    register(data: UserRegistrationRequest): Observable<UserResponse> {
        return this.http.post<UserResponse>(
            `${this.api.userUrl}/register`,
            data
        );
    }

    fetchUser(id: number): Observable<UserResponse> {
        return this.http.get<UserResponse>(`${this.api.userUrl}/${id}`);
    }

    // TODO: fetchUsers(), server endpoint not implemented

}