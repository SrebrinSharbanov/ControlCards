package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Model.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkshopRepository extends JpaRepository<WorkCenter, Integer> {

    Optional<Workshop> findById(Long id);
    Workshop save(Workshop workshop);
    void deleteById(Long id);
    boolean existsById(Long id);

    Optional<Workshop> findByName(String name);
    boolean existsByName(String name);
    List<Workshop> findByNameContainingIgnoreCase(String name);
    List<Workshop> findByDescriptionContainingIgnoreCase(String description);
}
