package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.ArchivedCard;
import com.ControlCards.ControlCards.Model.Card;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.ArchivedCardRepository;
import com.ControlCards.ControlCards.Repository.CardRepository;
import com.ControlCards.ControlCards.Service.CardService;
import com.ControlCards.ControlCards.Service.LogEntryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final ArchivedCardRepository archivedCardRepository;
    private final LogEntryService logEntryService;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository, ArchivedCardRepository archivedCardRepository, LogEntryService logEntryService) {
        this.cardRepository = cardRepository;
        this.archivedCardRepository = archivedCardRepository;
        this.logEntryService = logEntryService;
    }

    @Transactional
    public void archiveCard(Long cardId, User archivedBy) {
        // 1. Намери картата
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // 2. Създай архивирана карта
        ArchivedCard archivedCard = new ArchivedCard(card, archivedBy);
        archivedCardRepository.save(archivedCard);


        // 3. Изтрий оригиналната карта
        cardRepository.delete(card);
    }
}
