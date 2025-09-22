package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.Card;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Util.Enums.CardStatus;
import com.ControlCards.ControlCards.Util.Enums.Shift;

import java.util.List;
import java.util.Optional;

public interface CardService {

    // CRUD операции

    Optional<Card> findById(Long id);
    Card save(Card card);
    void deleteById(Long id);
    boolean existsById(Long id);

    // Специфични методи
    List<Card> findByWorkshopId(Long workshopId);
    List<Card> findByWorkCenterId(Integer workCenterId);
    List<Card> findByShift(Shift shift);
    List<Card> findByStatus(CardStatus status);
    List<Card> findByShortDescriptionContaining(String shortDescription);
    List<Card> findByDetailedDescriptionContaining(String detailedDescription);

    void archiveCard(Long cardId, User archivedBy);

}
