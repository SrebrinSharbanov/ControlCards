package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);
    List<User> findByFirstNameContainingIgnoreCase(String firstName);
    List<User> findByLastNameContainingIgnoreCase(String lastName);

    @Query("SELECT u FROM User u JOIN u.workshops w WHERE w.id = :workshopId")
    List<User> findByWorkshopId(@Param("workshopId") Long workshopId);
}