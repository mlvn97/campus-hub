package fr.esgi.event_api.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esgi.event_api.entities.Registration;
import fr.esgi.event_api.enums.RegistrationStatus;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Integer countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, RegistrationStatus status);

    Optional<Registration> findFirstByUserIdAndEventIdAndStatusIn(
            Long userId,
            Long eventId,
            Collection<RegistrationStatus> statuses);

    List<Registration> findByEventIdAndStatusIn(Long eventId, Collection<RegistrationStatus> statuses);

    Optional<Registration> findFirstByEventIdAndStatusOrderByRegistrationDateAsc(Long eventId, RegistrationStatus status);
}
