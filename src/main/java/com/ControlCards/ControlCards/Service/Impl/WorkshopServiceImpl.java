package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Repository.WorkshopRepository;
import com.ControlCards.ControlCards.Service.UserService;
import com.ControlCards.ControlCards.Service.WorkshopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final LogEntryService logEntryService;
    private final UserService userService;

    @Autowired
    public WorkshopServiceImpl(WorkshopRepository workshopRepository, LogEntryService logEntryService, UserService userService) {
        this.workshopRepository = workshopRepository;
        this.logEntryService = logEntryService;
        this.userService = userService;
    }

    @Override
    @Cacheable("workshops")
    public List<Workshop> findAll() {
        log.debug("Finding all workshops (including inactive) - cache miss, fetching from database");
        return workshopRepository.findAll();
    }

    @Override
    @Cacheable("activeWorkshops")
    public List<Workshop> findAllActive() {
        log.debug("Finding all active workshops - cache miss, fetching from database");
        return workshopRepository.findByActiveTrue();
    }

    @Override
    public Optional<Workshop> findById(UUID id) {
        return workshopRepository.findById(id);
    }

    @Override
    @CacheEvict(value = {"workshops", "activeWorkshops"}, allEntries = true)
    public Workshop save(Workshop workshop) {
        log.info("Saving workshop: {} - evicting cache", workshop.getName());
        boolean isNew = workshop.getId() == null;
        Workshop savedWorkshop = workshopRepository.save(workshop);
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                if (isNew) {
                    logEntryService.createLog(currentUser, "Създаден нов цех: " + savedWorkshop.getName());
                } else {
                    logEntryService.createLog(currentUser, "Обновен цех: " + savedWorkshop.getName());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to log action: {}", e.getMessage());
        }
        
        return savedWorkshop;
    }

    @Override
    public boolean existsById(UUID id) {
        return workshopRepository.existsById(id);
    }

    @Override
    @CacheEvict(value = {"workshops", "activeWorkshops"}, allEntries = true)
    public void deactivate(UUID id) {
        log.info("Deactivating workshop with ID: {} - evicting cache", id);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workshop not found: " + id));
        workshop.setActive(false);
        workshopRepository.save(workshop);
        log.info("Workshop {} deactivated", workshop.getName());
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                logEntryService.createLog(currentUser, "Деактивиран цех: " + workshop.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to log action: {}", e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = {"workshops", "activeWorkshops"}, allEntries = true)
    public void activate(UUID id) {
        log.info("Activating workshop with ID: {} - evicting cache", id);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workshop not found: " + id));
        workshop.setActive(true);
        workshopRepository.save(workshop);
        log.info("Workshop {} activated", workshop.getName());
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                logEntryService.createLog(currentUser, "Активиран цех: " + workshop.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to log action: {}", e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userService.findByUsername(userDetails.getUsername()).orElse(null);
            }
        } catch (Exception e) {
            log.debug("Cannot extract current user: {}", e.getMessage());
        }
        return null;
    }
}
