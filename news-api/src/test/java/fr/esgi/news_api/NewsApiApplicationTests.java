package fr.esgi.news_api;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=${NEWS_TEST_DB_URL}",
        "spring.datasource.username=${NEWS_TEST_DB_USERNAME}",
        "spring.datasource.password=${NEWS_TEST_DB_PASSWORD:}",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXowMTIzNDU="
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "NEWS_TEST_DB_URL", matches = ".+")
class NewsApiApplicationTests {
    private static final String BASE = "/api/actualites";
    private static final String PRESIDENT = token(10, "ADMIN_ASSOCIATION");
    private static final String OTHER_PRESIDENT = token(20, "ADMIN_ASSOCIATION");
    private static final String CAMPUS = token(30, "ADMIN_CAMPUS");
    private static final String STUDENT = token(40, "STUDENT");

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void sharedTables(@Autowired JdbcTemplate jdbc) {
        jdbc.execute("create table if not exists associations (id bigint primary key)");
        jdbc.execute("""
                create table if not exists memberships (
                    user_id bigint not null, association_id bigint not null, role varchar(32) not null,
                    primary key (user_id, association_id)
                )
                """);
        jdbc.update("insert into associations values (101), (202) on conflict do nothing");
        jdbc.update("""
                insert into memberships values (10, 101, 'PRESIDENT'), (20, 202, 'PRESIDENT')
                on conflict do nothing
                """);
    }

    @Test
    void managesNewsAndOnlyReturnsPublishedMatches() throws Exception {
        String news = create(PRESIDENT, "Culture", "101");
        String otherCategory = create(PRESIDENT, "Sport", "101");
        String otherAssociation = create(OTHER_PRESIDENT, "Culture", "202");

        mvc.perform(get(news).header("Authorization", STUDENT)).andExpect(status().isNotFound());
        mvc.perform(get(news).header("Authorization", PRESIDENT))
                .andExpect(jsonPath("$.status").value("BROUILLON"));
        mvc.perform(patch(news + "/publier").header("Authorization", OTHER_PRESIDENT))
                .andExpect(status().isForbidden());
        mvc.perform(patch(news + "/archiver").header("Authorization", PRESIDENT))
                .andExpect(status().isConflict());
        mvc.perform(patch(news).header("Authorization", PRESIDENT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Titre modifié\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Titre modifié"));

        publish(news, PRESIDENT);
        publish(otherCategory, CAMPUS);
        publish(otherAssociation, OTHER_PRESIDENT);
        mvc.perform(patch(news + "/publier").header("Authorization", PRESIDENT))
                .andExpect(status().isConflict());

        mvc.perform(get(BASE).header("Authorization", STUDENT).param("page", "2").param("limit", "1")
                        .param("categorie", "Culture"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items[0].associationId").value(101));
        mvc.perform(get(BASE).header("Authorization", STUDENT)
                        .param("categorie", "Culture").param("association", "101"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItems").value(1));

        mvc.perform(get(news).header("Authorization", STUDENT)).andExpect(status().isOk());
        mvc.perform(patch(news + "/archiver").header("Authorization", CAMPUS))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ARCHIVEE"));
        mvc.perform(get(BASE).header("Authorization", STUDENT)
                        .param("categorie", "Culture").param("association", "101"))
                .andExpect(jsonPath("$.totalItems").value(0));
        mvc.perform(get(news).header("Authorization", STUDENT)).andExpect(status().isNotFound());
        mvc.perform(delete(news).header("Authorization", PRESIDENT)).andExpect(status().isNoContent());
        mvc.perform(get(news).header("Authorization", CAMPUS)).andExpect(status().isNotFound());
    }

    @Test
    void checksCreationRightsAndValidatesInput() throws Exception {
        mvc.perform(post(BASE).header("Authorization", STUDENT).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Culture", "101")))
                .andExpect(status().isForbidden());
        mvc.perform(post(BASE).header("Authorization", PRESIDENT).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Culture", "202")))
                .andExpect(status().isForbidden());
        mvc.perform(post(BASE).header("Authorization", CAMPUS).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Culture", "303")))
                .andExpect(status().isNotFound());
        mvc.perform(post(BASE).header("Authorization", CAMPUS).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(BASE).header("Authorization", STUDENT).param("limit", "101"))
                .andExpect(status().isBadRequest());

        String campusNews = create(CAMPUS, "Campus", "null");
        mvc.perform(patch(campusNews + "/publier").header("Authorization", PRESIDENT))
                .andExpect(status().isForbidden());
        publish(campusNews, CAMPUS);
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    private String create(String actor, String category, String association) throws Exception {
        return mvc.perform(post(BASE).header("Authorization", actor).contentType(MediaType.APPLICATION_JSON)
                        .content(body(category, association)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BROUILLON"))
                .andReturn().getResponse().getHeader("Location");
    }

    private void publish(String location, String actor) throws Exception {
        mvc.perform(patch(location + "/publier").header("Authorization", actor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLIEE"))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty());
    }

    private static String body(String category, String association) {
        return """
                {"title":"Annonce","content":"Contenu","category":"%s","associationId":%s}
                """.formatted(category, association);
    }

    private static String token(long userId, String role) {
        SecretKey key = new SecretKeySpec(
                "abcdefghijklmnopqrstuvwxyz012345".getBytes(java.nio.charset.StandardCharsets.US_ASCII), "HmacSHA256");
        return "Bearer " + Jwts.builder().subject(Long.toString(userId)).claim("role", role)
                .expiration(Date.from(Instant.now().plusSeconds(600))).signWith(key).compact();
    }
}
