package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Repository.WorkCenterRepository;
import com.ControlCards.ControlCards.Service.UserService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkCenterServiceImpl implements WorkCenterService {

    private final WorkCenterRepository workCenterRepository;
    private final LogEntryService logEntryService;
    private final UserService userService;

    @Autowired
    public WorkCenterServiceImpl(WorkCenterRepository workCenterRepository, LogEntryService logEntryService, UserService userService) {
        this.workCenterRepository = workCenterRepository;
        this.logEntryService = logEntryService;
        this.userService = userService;
    }

    @Override
    @Cacheable("workCenters")
    public List<WorkCenter> findAll() {
        log.debug("Finding all work centers - cache miss, fetching from database");
        return workCenterRepository.findAll();
    }

    @Override
    @Cacheable("activeWorkCenters")
    public List<WorkCenter> findAllActive() {
        log.debug("Finding all active work centers - cache miss, fetching from database");
        return workCenterRepository.findAll().stream()
                .filter(wc -> wc.getActive() != null && wc.getActive())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WorkCenter> findById(UUID id) {
        return workCenterRepository.findById(id);
    }

    @Override
    @CacheEvict(value = {"workCenters", "activeWorkCenters"}, allEntries = true)
    public WorkCenter save(WorkCenter workCenter) {
        log.info("Saving work center: {} - evicting cache", workCenter.getNumber());
        boolean isNew = workCenter.getId() == null;
        WorkCenter savedWorkCenter = workCenterRepository.save(workCenter);
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                if (isNew) {
                    logEntryService.createLog(currentUser, "Създаден нов работен център: " + savedWorkCenter.getNumber());
                } else {
                    logEntryService.createLog(currentUser, "Обновен работен център: " + savedWorkCenter.getNumber());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to log action: {}", e.getMessage());
        }
        
        return savedWorkCenter;
    }

    @Override
    public boolean existsById(UUID id) {
        return workCenterRepository.existsById(id);
    }

    @Override
    public List<WorkCenter> findByWorkshopId(UUID workshopId) {
        log.debug("Finding work centers for workshop ID: {}", workshopId);
        return workCenterRepository.findByWorkshopId(workshopId);
    }

    @Override
    public List<WorkCenter> findByWorkshopIdWithWorkshop(UUID workshopId) {
        log.debug("Finding active work centers with workshop for workshop ID: {}", workshopId);
        return workCenterRepository.findWithWorkshopByWorkshopIdAndActiveTrue(workshopId);
    }

    @Override
    @CacheEvict(value = {"workCenters", "activeWorkCenters"}, allEntries = true)
    public void deactivate(UUID id) {
        log.info("Deactivating work center with ID: {} - evicting cache", id);
        WorkCenter workCenter = workCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work center not found: " + id));
        workCenter.setActive(false);
        workCenterRepository.save(workCenter);
        log.info("Work center {} deactivated", workCenter.getNumber());
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                logEntryService.createLog(currentUser, "Деактивиран работен център: " + workCenter.getNumber());
            }
        } catch (Exception e) {
            log.warn("Failed to log action: {}", e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = {"workCenters", "activeWorkCenters"}, allEntries = true)
    public void activate(UUID id) {
        log.info("Activating work center with ID: {} - evicting cache", id);
        WorkCenter workCenter = workCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work center not found: " + id));
        workCenter.setActive(true);
        workCenterRepository.save(workCenter);
        log.info("Work center {} activated", workCenter.getNumber());
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                logEntryService.createLog(currentUser, "Активиран работен център: " + workCenter.getNumber());
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
