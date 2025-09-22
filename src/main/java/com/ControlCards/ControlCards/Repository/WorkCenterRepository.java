package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.WorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkCenterRepository extends JpaRepository<WorkCenter, Integer> {
    Optional<WorkCenter> findByNumber(String number);
    boolean existsByNumber(String number);
    List<WorkCenter> findByNumberContainingIgnoreCase(String number);
    List<WorkCenter> findByDescriptionContainingIgnoreCase(String description);
    List<WorkCenter> findByMachineTypeContainingIgnoreCase(String machineType);

    @Query("SELECT w FROM WorkCenter w WHERE w.workshop.id = :workshopId")
    List<WorkCenter> findByWorkshopId(@Param("workshopId") Long workshopId);
}
