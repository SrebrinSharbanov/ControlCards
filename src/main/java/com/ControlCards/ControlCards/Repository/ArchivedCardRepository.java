package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.ArchivedCard;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArchivedCardRepository extends JpaRepository<ArchivedCard, UUID> {
    
    @EntityGraph(attributePaths = {"workshop", "workCenter", "createdBy", "updatedBy", "archivedBy"})
    @Override
    List<ArchivedCard> findAll();
}

