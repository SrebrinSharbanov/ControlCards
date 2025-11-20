package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.Workshop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkshopService {
    List<Workshop> findAll(); // Returns all workshops (including inactive) - for admin
    List<Workshop> findAllActive(); // Returns only active workshops
    Optional<Workshop> findById(UUID id);
    Workshop save(Workshop workshop);
    void deactivate(UUID id);
    void activate(UUID id);
    boolean existsById(UUID id);
}

