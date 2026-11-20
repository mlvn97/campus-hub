package fr.esgi.event_api.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import fr.esgi.event_api.entities.Event;
import fr.esgi.event_api.enums.EventStatus;

public class EventSpecification {

    protected static Specification<Event> withFilters(EventStatus status, Long associationId, LocalDateTime date) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (associationId != null) predicates.add(cb.equal(root.get("associationId"), associationId));
            if (date != null) predicates.add(cb.equal(root.get("date"), date));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
}
