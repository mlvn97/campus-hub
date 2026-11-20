package fr.esgi.event_api.repositories;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.esgi.event_api.entities.Event;

@Repository 
public interface EventRepository extends JpaRepository<Event, Long> {
    
    @Modifying 
    @Query ("""
        UPDATE Event e
        SET e.status = 'OVER'
        WHERE e.date < :now
        AND e.status <> 'CANCELLED'
        AND e.status <> 'DRAFT'
    """)
    public Integer updateFinishedEvents(@Param("now") LocalDateTime now);
}
