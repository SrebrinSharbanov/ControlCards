package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.WorkCenter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkCenterService {
    List<WorkCenter> findAll(); // Returns all work centers (including inactive) - for admin
    List<WorkCenter> findAllActive(); // Returns only active work centers
    Optional<WorkCenter> findById(UUID id);
    WorkCenter save(WorkCenter workCenter);
    void deactivate(UUID id);
    void activate(UUID id);
    boolean existsById(UUID id);
    List<WorkCenter> findByWorkshopId(UUID workshopId);
    List<WorkCenter> findByWorkshopIdWithWorkshop(UUID workshopId);
}

