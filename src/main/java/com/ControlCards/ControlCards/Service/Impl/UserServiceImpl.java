package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.UserRepository;
import com.ControlCards.ControlCards.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.ControlCards.ControlCards.Util.Enums.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LogEntryService logEntryService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, LogEntryService logEntryService) {
        this.userRepository = userRepository;
        this.logEntryService = logEntryService;
    }

    @Override
    public List<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByUsernameWithWorkshops(String username) {
        return userRepository.findWithWorkshopsByUsername(username);
    }

    @Override
    public User save(User user) {
        log.info("Saving user: {}", user.getUsername());
        boolean isNew = user.getId() == null;
        User savedUser = userRepository.save(user);
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                if (isNew) {
                    logEntryService.createLog(currentUser, "Създаден нов потребител: " + savedUser.getUsername());
                } else {
                    logEntryService.createLog(currentUser, "Обновен потребител: " + savedUser.getUsername());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to log action: {}", e.getMessage());
        }
        
        return savedUser;
    }
    
    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return findByUsername(userDetails.getUsername()).orElse(null);
            }
        } catch (Exception e) {
            log.debug("Cannot extract current user: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    @Override
    public void deactivate(UUID id) {
        log.info("Deactivating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        
        if (user.getRole() == Role.ADMIN) {
            log.warn("Cannot deactivate ADMIN user: {}", user.getUsername());
            throw new RuntimeException("Cannot deactivate ADMIN user");
        }
        
        user.setActive(false);
        userRepository.save(user);
        log.info("User {} deactivated", user.getUsername());
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                logEntryService.createLog(currentUser, "Деактивиран потребител: " + user.getUsername());
            }
        } catch (Exception e) {
            log.warn("Failed to log action: {}", e.getMessage());
        }
    }

    @Override
    public void activate(UUID id) {
        log.info("Activating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(true);
        userRepository.save(user);
        log.info("User {} activated", user.getUsername());
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                logEntryService.createLog(currentUser, "Активиран потребител: " + user.getUsername());
            }
        } catch (Exception e) {
            log.warn("Failed to log action: {}", e.getMessage());
        }
    }
}
