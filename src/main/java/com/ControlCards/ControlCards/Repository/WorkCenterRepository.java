package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.WorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkCenterRepository extends JpaRepository<WorkCenter, UUID> {
    List<WorkCenter> findByWorkshopId(UUID workshopId);
}
