package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    User save(User user);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}

