package fr.esgi.user_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esgi.user_api.models.User;

/**
 * UserRepository
 */
@Repository 
public interface UserRepository extends JpaRepository<User, Long> {
    
    public <T extends User> T save(User user);

    public boolean existsByEmail(String email);

}
