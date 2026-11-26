package fr.esgi.news_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import fr.esgi.news_api.dto.NewsResponse;
import fr.esgi.news_api.dto.UpdateNewsRequest;
import fr.esgi.news_api.entities.News;
import fr.esgi.news_api.entities.NewsStatus;
import fr.esgi.news_api.repositories.NewsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class NewsServiceTest {
    private static final String AUTHORIZATION = "Bearer test-token";

    @Test
    void editsAndRepublishesArchivedNews() {
        NewsRepository repository = mock(NewsRepository.class);
        NewsAccess access = mock(NewsAccess.class);
        Authentication actor = mock(Authentication.class);
        News archived = new News("Ancien titre", "Contenu", "Campus", null, 42L);
        archived.archive();
        when(repository.findForUpdate(1L)).thenReturn(Optional.of(archived));
        when(access.canManage(actor, null, AUTHORIZATION)).thenReturn(true);
        when(access.isCampusAdmin(actor)).thenReturn(false);
        when(actor.getName()).thenReturn("42");
        NewsService service = new NewsService(repository, access);

        NewsResponse edited = service.update(1L,
                new UpdateNewsRequest("Titre corrigé", null, null), actor, AUTHORIZATION);

        assertEquals("Titre corrigé", edited.title());
        assertEquals(NewsStatus.ARCHIVEE, edited.status());

        NewsResponse republished = service.publish(1L, actor, AUTHORIZATION);

        assertEquals(NewsStatus.PUBLIEE, republished.status());
        assertNotNull(republished.publishedAt());
    }
}
