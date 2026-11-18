package fr.esgi.event_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esgi.event_api.entities.Event;

@Repository 
public interface EventRepository extends JpaRepository<Event, Long> {
    
}
