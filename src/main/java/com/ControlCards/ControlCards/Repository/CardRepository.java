package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.Card;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Util.Enums.CardStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    
    @EntityGraph(attributePaths = {"workshop", "workCenter", "createdBy", "updatedBy", "extendedBy", "closedBy"})
    @Override
    List<Card> findAll();
    
    @EntityGraph(attributePaths = {"workshop", "workCenter", "createdBy", "updatedBy", "extendedBy", "closedBy"})
    List<Card> findByStatus(CardStatus status);
    
    @EntityGraph(attributePaths = {"workshop", "workCenter", "createdBy", "updatedBy", "extendedBy", "closedBy"})
    List<Card> findByStatusAndWorkshopIn(CardStatus status, List<Workshop> workshops);
    
    @EntityGraph(attributePaths = {"workshop", "workCenter", "createdBy", "updatedBy", "extendedBy", "closedBy"})
    List<Card> findByWorkshopIn(List<Workshop> workshops);
}
