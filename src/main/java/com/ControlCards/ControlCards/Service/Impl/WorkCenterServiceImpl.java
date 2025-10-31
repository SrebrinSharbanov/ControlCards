package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Repository.WorkCenterRepository;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkCenterServiceImpl implements WorkCenterService {

    private final WorkCenterRepository workCenterRepository;

    @Autowired
    public WorkCenterServiceImpl(WorkCenterRepository workCenterRepository) {
        this.workCenterRepository = workCenterRepository;
    }

    @Override
    public List<WorkCenter> findAll() {
        return workCenterRepository.findAll();
    }

    @Override
    public Optional<WorkCenter> findById(UUID id) {
        return workCenterRepository.findById(id);
    }

    @Override
    public WorkCenter save(WorkCenter workCenter) {
        return workCenterRepository.save(workCenter);
    }

    @Override
    public void deleteById(UUID id) {
        workCenterRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return workCenterRepository.existsById(id);
    }

    @Override
    public List<WorkCenter> findByWorkshopId(UUID workshopId) {
        return workCenterRepository.findByWorkshopId(workshopId);
    }
}

