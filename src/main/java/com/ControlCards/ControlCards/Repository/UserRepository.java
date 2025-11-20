package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    
    @EntityGraph(attributePaths = {"workshops"})
    Optional<User> findWithWorkshopsByUsername(String username);
}
