package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
}
