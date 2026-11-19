package fr.esgi.news_api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateNewsRequest(
        @Pattern(regexp = "(?sU).*\\S.*") @Size(max = 200) String title,
        @Pattern(regexp = "(?sU).*\\S.*") @Size(max = 50_000) String content,
        @Pattern(regexp = "(?sU).*\\S.*") @Size(max = 80) String category
) {}
