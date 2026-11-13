package fr.esgi.user_api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esgi.user_api.models.User;

/**
 * UserRepository
 */
@Repository 
public interface UserRepository extends JpaRepository<User, Long> {
    
    public boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}
