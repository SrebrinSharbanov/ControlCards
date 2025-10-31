package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Optional<User> findByUsername(String username);
}

