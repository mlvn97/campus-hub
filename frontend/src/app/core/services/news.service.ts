import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { NewsFeedResponse } from '../../dto/news/news-feed-response.dto';
import { CreateNewsRequest, UpdateNewsRequest } from '../../dto/news/news-request.dto';
import { News } from '../../models/news.model';
import { ApiService } from './api.service';
import { queryParams } from './query-params';

export interface NewsFilters {
    page?: number;
    limit?: number;
    category?: string;
    associationId?: number;
}

@Injectable({
    providedIn: 'root'
})
export class NewsService {
    private readonly url: string;

    constructor(private http: HttpClient, api: ApiService) {
        this.url = api.newsUrl;
    }

    fetchFeed({ page, limit, category, associationId }: NewsFilters = {}): Observable<NewsFeedResponse> {
        return this.http.get<NewsFeedResponse>(this.url, {
            params: queryParams({ page, limit, categorie: category, association: associationId })
        });
    }

    fetchManaged({ page, limit }: NewsFilters = {}): Observable<NewsFeedResponse> {
        return this.http.get<NewsFeedResponse>(`${this.url}/gestion`, {
            params: queryParams({ page, limit })
        });
    }

    fetchNews(id: number): Observable<News> {
        return this.http.get<News>(`${this.url}/${id}`);
    }

    createNews(data: CreateNewsRequest): Observable<News> {
        return this.http.post<News>(this.url, data);
    }

    updateNews(id: number, data: UpdateNewsRequest): Observable<News> {
        return this.http.patch<News>(`${this.url}/${id}`, data);
    }

    deleteNews(id: number): Observable<void> {
        return this.http.delete<void>(`${this.url}/${id}`);
    }

    publishNews(id: number): Observable<News> {
        return this.http.patch<News>(`${this.url}/${id}/publier`, null);
    }

    archiveNews(id: number): Observable<News> {
        return this.http.patch<News>(`${this.url}/${id}/archiver`, null);
    }
}
