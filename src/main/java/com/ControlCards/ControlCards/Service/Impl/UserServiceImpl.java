package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.UserRepository;
import com.ControlCards.ControlCards.Service.LogEntryService;
import com.ControlCards.ControlCards.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final LogEntryService logEntryService;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(LogEntryService logEntryService, UserRepository userRepository) {
        this.logEntryService = logEntryService;
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User save(User user) {

        // Logging
        User currentUser = getCurrentUser();
        LogEntry log = logEntryService.createLog(currentUser, "Създаден потребител: " + user.getUsername());
        logEntryService.saveLog(log);

        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            //Logging
            User currentUser = getCurrentUser();
            LogEntry log = logEntryService.createLog(currentUser, "Изтрит потребител: " + user.getUsername());
            logEntryService.saveLog(log);

            userRepository.deleteById(id);
        }

    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public List<User> findByUsernameContaining(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    @Override
    public List<User> findByFirstNameContaining(String firstName) {
        return userRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    @Override
    public List<User> findByLastNameContaining(String lastName) {
        return userRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    @Override
    public List<User> findByWorkshopId(Long workshopId) {
        return userRepository.findByWorkshopId(workshopId);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
