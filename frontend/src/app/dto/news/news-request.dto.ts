import { News } from '../../models/news.model';

export type UpdateNewsRequest = Partial<Pick<News, 'title' | 'content' | 'category'>>;

export interface CreateNewsRequest extends Required<UpdateNewsRequest> {
    associationId?: number | null;
}
