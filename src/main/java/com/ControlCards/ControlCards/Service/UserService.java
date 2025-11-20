package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    List<User> findAll(); // Returns all users (including inactive) - for admin
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameWithWorkshops(String username);
    User save(User user);
    void deactivate(UUID id);
    void activate(UUID id);
    boolean existsById(UUID id);
}

