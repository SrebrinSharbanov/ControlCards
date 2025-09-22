package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.WorkCenter;

import java.util.List;
import java.util.Optional;

public interface WorkCenterService {
    // CRUD операции

    List<WorkCenter> findAll();
    Optional<WorkCenter> findById(Integer id);
    WorkCenter save(WorkCenter workCenter);
    void deleteById(Integer id);
    boolean existsById(Integer id);

    // Специфични методи
    Optional<WorkCenter> findByNumber(String number);
    boolean existsByNumber(String number);
    List<WorkCenter> findByNumberContaining(String number);
    List<WorkCenter> findByDescriptionContaining(String description);
    List<WorkCenter> findByMachineTypeContaining(String machineType);
    List<WorkCenter> findByWorkshopId(Long workshopId);
}
