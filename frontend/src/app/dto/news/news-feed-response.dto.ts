import { News } from '../../models/news.model';

export interface NewsFeedResponse {
    items: News[];
    page: number;
    limit: number;
    totalItems: number;
    totalPages: number;
}
