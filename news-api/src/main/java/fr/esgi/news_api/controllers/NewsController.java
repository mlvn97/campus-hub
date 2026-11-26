package fr.esgi.news_api.controllers;

import java.net.URI;

import fr.esgi.news_api.dto.CreateNewsRequest;
import fr.esgi.news_api.dto.NewsFeedResponse;
import fr.esgi.news_api.dto.NewsResponse;
import fr.esgi.news_api.dto.UpdateNewsRequest;
import fr.esgi.news_api.services.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
@Tag(name = "Actualités")
@SecurityRequirement(name = "bearerAuth")
public class NewsController {
    private final NewsService service;

    @GetMapping
    @Operation(summary = "Consulter le fil des actualités publiées")
    public NewsFeedResponse feed(
            @RequestParam(defaultValue = "1") @Positive int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int limit,
            @RequestParam(name = "categorie", required = false) @Size(min = 1, max = 80) String category,
            @RequestParam(name = "association", required = false) @Positive Long associationId) {
        return service.feed(page, limit, category, associationId);
    }

    @GetMapping("/gestion")
    @Operation(summary = "Consulter les actualités que l'utilisateur peut gérer")
    public NewsFeedResponse managedFeed(
            @RequestParam(defaultValue = "1") @Positive int page,
            @RequestParam(defaultValue = "20") @Positive @Max(100) int limit,
            Authentication actor) {
        return service.managedFeed(page, limit, actor);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une actualité")
    public NewsResponse get(@PathVariable @Positive long id, Authentication actor,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return service.get(id, actor, authorization);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un brouillon")
    public ResponseEntity<NewsResponse> create(@Valid @RequestBody CreateNewsRequest request, Authentication actor,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        NewsResponse news = service.create(request, actor, authorization);
        return ResponseEntity.created(URI.create("/api/actualites/" + news.id())).body(news);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier le titre, le contenu ou la catégorie")
    public NewsResponse update(@PathVariable @Positive long id,
            @Valid @RequestBody UpdateNewsRequest request, Authentication actor,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return service.update(id, request, actor, authorization);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une actualité")
    public void delete(@PathVariable @Positive long id, Authentication actor,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        service.delete(id, actor, authorization);
    }

    @PatchMapping("/{id}/publier")
    @Operation(summary = "Publier ou republier une actualité")
    public NewsResponse publish(@PathVariable @Positive long id, Authentication actor,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return service.publish(id, actor, authorization);
    }

    @PatchMapping("/{id}/archiver")
    @Operation(summary = "Archiver une actualité publiée")
    public NewsResponse archive(@PathVariable @Positive long id, Authentication actor,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return service.archive(id, actor, authorization);
    }
}
