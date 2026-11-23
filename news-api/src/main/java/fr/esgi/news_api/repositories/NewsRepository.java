package fr.esgi.news_api.repositories;

import java.util.Optional;

import fr.esgi.news_api.entities.News;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from News n where n.id = :id")
    Optional<News> findForUpdate(@Param("id") long id);
}
