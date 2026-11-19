package fr.esgi.news_api.dto;

import java.util.List;

public record NewsFeedResponse(
        List<NewsResponse> items, int page, int limit, long totalItems, int totalPages
) {}
