package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.Card;
import com.ControlCards.ControlCards.Util.Enums.CardStatus;
import com.ControlCards.ControlCards.Util.Enums.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("SELECT c FROM Card c WHERE c.workshop.id = :workshopId")
    List<Card> findByWorkshopId(@Param("workshopId") Long workshopId);

    @Query("SELECT c FROM Card c WHERE c.workCenter.id = :workCenterId")
    List<Card> findByWorkCenterId(@Param("workCenterId") Integer workCenterId);

    List<Card> findByShift(Shift shift);
    List<Card> findByStatus(CardStatus status);
    List<Card> findByShortDescriptionContainingIgnoreCase(String shortDescription);
    List<Card> findByDetailedDescriptionContainingIgnoreCase(String detailedDescription);
}
