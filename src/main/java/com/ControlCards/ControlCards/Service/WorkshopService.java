package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.Workshop;

import java.util.List;
import java.util.Optional;

public interface WorkshopService {
    // CRUD операции

    Optional<Workshop> findById(Long id);
    Workshop save(Workshop workshop);
    void deleteById(Long id);
    boolean existsById(Long id);

    // Специфични методи
    Optional<Workshop> findByName(String name);
    boolean existsByName(String name);
    List<Workshop> findByNameContaining(String name);
    List<Workshop> findByDescriptionContaining(String description);
}
