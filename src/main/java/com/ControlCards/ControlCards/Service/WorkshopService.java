package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.Workshop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkshopService {
    List<Workshop> findAll();
    Optional<Workshop> findById(UUID id);
    Workshop save(Workshop workshop);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}

