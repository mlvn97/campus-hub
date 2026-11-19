package fr.esgi.news_api.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "news", indexes = @Index(name = "idx_news_feed", columnList = "status, published_at, id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "association_id", updatable = false)
    private Long associationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NewsStatus status = NewsStatus.BROUILLON;

    @Column(name = "published_at")
    private Instant publishedAt;

    public News(String title, String content, String category, Long associationId) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.associationId = associationId;
    }

    public void edit(String title, String content, String category) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (category != null) {
            this.category = category;
        }
    }

    public void publish(Instant publishedAt) {
        this.publishedAt = publishedAt;
        status = NewsStatus.PUBLIEE;
    }

    public void archive() {
        status = NewsStatus.ARCHIVEE;
    }
}
