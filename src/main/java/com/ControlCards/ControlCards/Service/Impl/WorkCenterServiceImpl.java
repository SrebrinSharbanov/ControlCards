package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Repository.WorkCenterRepository;
import com.ControlCards.ControlCards.Service.LogEntryService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkCenterServiceImpl implements WorkCenterService {

    private final LogEntryService logEntryService;
    private final WorkCenterRepository workCenterRepository;

    @Autowired
    public WorkCenterServiceImpl(LogEntryService logEntryService, WorkCenterRepository workCenterRepository) {
        this.logEntryService = logEntryService;
        this.workCenterRepository = workCenterRepository;
    }

    @Override
    public List<WorkCenter> findAll() {
        return workCenterRepository.findAll();
    }

    @Override
    public Optional<WorkCenter> findById(Integer id) {
        return workCenterRepository.findById(id);
    }

    @Override
    public WorkCenter save(WorkCenter workCenter) {

        // Logging
        User currentUser = getCurrentUser();
        LogEntry log = logEntryService.createLog(currentUser, "Създаден работен център: " + workCenter.getNumber());
        logEntryService.saveLog(log);

        return workCenterRepository.save(workCenter);
    }

    @Override
    public void deleteById(Integer id) {
        Optional<WorkCenter> workCenterOpt = workCenterRepository.findById(id);
        if (workCenterOpt.isPresent()) {
            WorkCenter workCenter = workCenterOpt.get();

            //Logging
            User currentUser = getCurrentUser();
            LogEntry log = logEntryService.createLog(currentUser, "Изтрит работен център: " + workCenter.getNumber());
            logEntryService.saveLog(log);

            workCenterRepository.deleteById(id);
        }

    }

    @Override
    public boolean existsById(Integer id) {
        return workCenterRepository.existsById(id);
    }

    @Override
    public Optional<WorkCenter> findByNumber(String number) {
        return workCenterRepository.findByNumber(number);
    }

    @Override
    public boolean existsByNumber(String number) {
        return workCenterRepository.existsByNumber(number);
    }

    @Override
    public List<WorkCenter> findByNumberContaining(String number) {
        return workCenterRepository.findByNumberContainingIgnoreCase(number);
    }

    @Override
    public List<WorkCenter> findByDescriptionContaining(String description) {
        return workCenterRepository.findByDescriptionContainingIgnoreCase(description);
    }

    @Override
    public List<WorkCenter> findByMachineTypeContaining(String machineType) {
        return workCenterRepository.findByMachineTypeContainingIgnoreCase(machineType);
    }

    @Override
    public List<WorkCenter> findByWorkshopId(Long workshopId) {
        return workCenterRepository.findByWorkshopId(workshopId);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
