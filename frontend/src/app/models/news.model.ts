export type NewsStatus = 'BROUILLON' | 'PUBLIEE' | 'ARCHIVEE';

export interface News {
    id: number;
    title: string;
    content: string;
    category: string;
    associationId: number | null;
    status: NewsStatus;
    publishedAt: string | null;
}
