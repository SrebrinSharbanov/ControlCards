package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.WorkCenter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkCenterService {
    List<WorkCenter> findAll();
    Optional<WorkCenter> findById(UUID id);
    WorkCenter save(WorkCenter workCenter);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    List<WorkCenter> findByWorkshopId(UUID workshopId);
}

