package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.ArchivedCard;
import com.ControlCards.ControlCards.Model.Card;
import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.CardRepository;
import com.ControlCards.ControlCards.Service.ArchivedCardService;
import com.ControlCards.ControlCards.Service.CardService;
import com.ControlCards.ControlCards.Service.LogEntryService;
import com.ControlCards.ControlCards.Util.Enums.CardStatus;
import com.ControlCards.ControlCards.Util.Enums.Shift;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final ArchivedCardService archivedCardService;
    private final LogEntryService logEntryService;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository, ArchivedCardService archivedCardService, LogEntryService logEntryService) {
        this.cardRepository = cardRepository;
        this.archivedCardService = archivedCardService;
        this.logEntryService = logEntryService;
    }

    @Override
    public Optional<Card> findById(Long id) {
        return cardRepository.findById(id);
    }

    @Override
    public Card save(Card card) {
        User currentUser = getCurrentUser();
        boolean isNew = card.getId() == null;

        if (isNew) {
            card.setCreatedBy(currentUser);
        }
        card.setUpdatedBy(currentUser);
        card.setUpdatedAt(LocalDateTime.now());

        // Logging
        String action = isNew ? "Създадена карта: " : "Обновена карта: ";
        LogEntry log = logEntryService.createLog(currentUser, action + card.getShortDescription());
        logEntryService.saveLog(log);

        return cardRepository.save(card);
    }

    @Override
    public void deleteById(Long id) {
        Optional<Card> cardOpt = cardRepository.findById(id);
        if (cardOpt.isPresent()) {
            Card card = cardOpt.get();

            //Logging
            User currentUser = getCurrentUser();
            LogEntry log = logEntryService.createLog(currentUser, "Изтрита карта: " + card.getShortDescription());
            logEntryService.saveLog(log);

            cardRepository.deleteById(id);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return cardRepository.existsById(id);
    }

    @Override
    public List<Card> findByWorkshopId(Long workshopId) {
        return cardRepository.findByWorkshopId(workshopId);
    }

    @Override
    public List<Card> findByWorkCenterId(Integer workCenterId) {
        return cardRepository.findByWorkCenterId(workCenterId);
    }

    @Override
    public List<Card> findByShift(Shift shift) {
        return cardRepository.findByShift(shift);
    }

    @Override
    public List<Card> findByStatus(CardStatus status) {
        return cardRepository.findByStatus(status);
    }

    @Override
    public List<Card> findByShortDescriptionContaining(String shortDescription) {
        return cardRepository.findByShortDescriptionContainingIgnoreCase(shortDescription);
    }

    @Override
    public List<Card> findByDetailedDescriptionContaining(String detailedDescription) {
        return cardRepository.findByDetailedDescriptionContainingIgnoreCase(detailedDescription);
    }

    @Transactional
    public void archiveCard(Long cardId, User archivedBy) {
        // 1. Намери картата
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // Logging
        LogEntry log = logEntryService.createLog(archivedBy, "Архивирана карта: " + card.getShortDescription());
        logEntryService.saveLog(log);

        // 2. Създай архивирана карта
        ArchivedCard archivedCard = new ArchivedCard(card, archivedBy);
        archivedCardService.save(archivedCard);

        // 3. Изтрий оригиналната карта
        cardRepository.delete(card);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
