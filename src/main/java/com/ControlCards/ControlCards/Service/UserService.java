package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    // CRUD операции

    List<User> findAll();
    Optional<User> findById(Long id);
    User save(User user);
    void deleteById(Long id);
    boolean existsById(Long id);

    // Специфични методи
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByUsernameContaining(String username);
    List<User> findByFirstNameContaining(String firstName);
    List<User> findByLastNameContaining(String lastName);
    List<User> findByWorkshopId(Long workshopId);
}
