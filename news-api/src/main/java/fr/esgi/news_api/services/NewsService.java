package fr.esgi.news_api.services;

import java.time.Instant;
import java.util.ArrayList;

import fr.esgi.news_api.dto.CreateNewsRequest;
import fr.esgi.news_api.dto.NewsFeedResponse;
import fr.esgi.news_api.dto.NewsResponse;
import fr.esgi.news_api.dto.UpdateNewsRequest;
import fr.esgi.news_api.entities.News;
import fr.esgi.news_api.entities.NewsStatus;
import fr.esgi.news_api.repositories.NewsRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {
    private final NewsRepository repository;
    private final NewsAccess access;

    public NewsFeedResponse feed(int page, int limit, String category, Long associationId) {
        if ((page - 1L) * limit > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page trop éloignée");
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "publishedAt", "id");
        Specification<News> filters = (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get("status"), NewsStatus.PUBLIEE));
            if (category != null) {
                predicates.add(builder.equal(root.get("category"), category));
            }
            if (associationId != null) {
                predicates.add(builder.equal(root.get("associationId"), associationId));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        var result = repository.findAll(filters, PageRequest.of(page - 1, limit, sort));
        return new NewsFeedResponse(result.map(NewsResponse::from).getContent(),
                page, limit, result.getTotalElements(), result.getTotalPages());
    }

    public NewsResponse get(long id, Authentication actor) {
        News news = repository.findById(id).orElseThrow(NewsService::notFound);
        if (news.getStatus() != NewsStatus.PUBLIEE && !access.canManage(actor, news.getAssociationId())) {
            throw notFound();
        }
        return NewsResponse.from(news);
    }

    @Transactional
    public NewsResponse create(CreateNewsRequest request, Authentication actor) {
        Long associationId = request.associationId();
        requireManager(actor, associationId);
        if (associationId != null && !access.associationExists(associationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Association introuvable");
        }
        return NewsResponse.from(repository.save(
                new News(request.title(), request.content(), request.category(), associationId)));
    }

    @Transactional
    public NewsResponse update(long id, UpdateNewsRequest request, Authentication actor) {
        News news = managedNews(id, actor);
        news.edit(request.title(), request.content(), request.category());
        return NewsResponse.from(news);
    }

    @Transactional
    public void delete(long id, Authentication actor) {
        repository.delete(managedNews(id, actor));
    }

    @Transactional
    public NewsResponse publish(long id, Authentication actor) {
        News news = managedNews(id, actor);
        requireStatus(news, NewsStatus.BROUILLON);
        news.publish(Instant.now());
        return NewsResponse.from(news);
    }

    @Transactional
    public NewsResponse archive(long id, Authentication actor) {
        News news = managedNews(id, actor);
        requireStatus(news, NewsStatus.PUBLIEE);
        news.archive();
        return NewsResponse.from(news);
    }

    private News managedNews(long id, Authentication actor) {
        News news = repository.findForUpdate(id).orElseThrow(NewsService::notFound);
        requireManager(actor, news.getAssociationId());
        return news;
    }

    private void requireManager(Authentication actor, Long associationId) {
        if (!access.canManage(actor, associationId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private static void requireStatus(News news, NewsStatus expected) {
        if (news.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transition incompatible avec le statut actuel");
        }
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Actualité introuvable");
    }
}
