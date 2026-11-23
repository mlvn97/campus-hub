package fr.esgi.news_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateNewsRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 50_000) String content,
        @NotBlank @Size(max = 80) String category,
        @Positive Long associationId
) {}
