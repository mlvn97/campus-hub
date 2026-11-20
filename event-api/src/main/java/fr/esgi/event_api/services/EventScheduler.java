package fr.esgi.event_api.services;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.esgi.event_api.repositories.EventRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class EventScheduler {
    
    private final EventRepository eventRepository;

    @Scheduled(fixedRate = 60000)  // Chaque minute
    @Transactional 
    public void updateFinishedEvents() {
        eventRepository.updateFinishedEvents(LocalDateTime.now());
    }
}
