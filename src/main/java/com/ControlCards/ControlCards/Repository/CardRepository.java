package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.Card;
import com.ControlCards.ControlCards.Util.Enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByStatus(CardStatus status);
}
