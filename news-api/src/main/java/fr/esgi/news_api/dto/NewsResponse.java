package fr.esgi.news_api.dto;

import java.time.Instant;

import fr.esgi.news_api.entities.News;
import fr.esgi.news_api.entities.NewsStatus;

public record NewsResponse(
        Long id, String title, String content, String category, Long associationId,
        NewsStatus status, Instant publishedAt
) {
    public static NewsResponse from(News news) {
        return new NewsResponse(news.getId(), news.getTitle(), news.getContent(), news.getCategory(),
                news.getAssociationId(), news.getStatus(), news.getPublishedAt());
    }
}
