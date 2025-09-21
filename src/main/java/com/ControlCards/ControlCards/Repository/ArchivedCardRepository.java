package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.ArchivedCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedCardRepository extends JpaRepository<ArchivedCard, Long> {
}
