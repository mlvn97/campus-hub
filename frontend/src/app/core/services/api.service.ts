import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";


@Injectable({
    providedIn: 'root'
})
export class ApiService {

    readonly userUrl = environment.api.user;
    readonly associationurl = environment.api.association;
    readonly eventUrl = environment.api.event;
    readonly newsUrl = environment.api.news;
}