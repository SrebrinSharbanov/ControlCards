package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Repository.WorkshopRepository;
import com.ControlCards.ControlCards.Service.LogEntryService;
import com.ControlCards.ControlCards.Service.WorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkshopServiceImpl implements WorkshopService {

    private final LogEntryService logEntryService;
    private final WorkshopRepository workshopRepository;

    @Autowired
    public WorkshopServiceImpl(LogEntryService logEntryService, WorkshopRepository workshopRepository) {
        this.logEntryService = logEntryService;
        this.workshopRepository = workshopRepository;
    }

    

    @Override
    public Optional<Workshop> findById(Long id) {
        return workshopRepository.findById(id);
    }

    @Override
    public Workshop save(Workshop workshop) {

        // Logging
        User currentUser = getCurrentUser();
        LogEntry log = logEntryService.createLog(currentUser, "Създаден цех: " + workshop.getName());
        logEntryService.saveLog(log);

        return workshopRepository.save(workshop);
    }



    @Override
    public void deleteById(Long id) {
        Optional<Workshop> workshopOpt = workshopRepository.findById(id);
        if (workshopOpt.isPresent()) {
            Workshop workshop = workshopOpt.get();

            //Logging
            User currentUser = getCurrentUser();
            LogEntry log = logEntryService.createLog(currentUser, "Изтрит цех: " + workshop.getName());
            logEntryService.saveLog(log);

            workshopRepository.deleteById(id);
        }

    }

    @Override
    public boolean existsById(Long id) {
        return workshopRepository.existsById(id);
    }


    @Override
    public Optional<Workshop> findByName(String name) {
        return workshopRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return workshopRepository.existsByName(name);
    }

    @Override
    public List<Workshop> findByNameContaining(String name) {
        return workshopRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Workshop> findByDescriptionContaining(String description) {
        return workshopRepository.findByDescriptionContainingIgnoreCase(description);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
