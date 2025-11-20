package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, UUID> {
    
    @Modifying
    @Query("DELETE FROM LogEntry l WHERE l.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT COUNT(l) FROM LogEntry l WHERE l.createdAt < :cutoffDate")
    long countByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
