package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.DTO.CardCreateDTO;
import com.ControlCards.ControlCards.DTO.CardViewDTO;
import com.ControlCards.ControlCards.Exception.CardNotFoundException;
import com.ControlCards.ControlCards.Exception.InvalidCardStatusException;
import com.ControlCards.ControlCards.Model.ArchivedCard;
import com.ControlCards.ControlCards.Model.Card;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Repository.CardRepository;
import com.ControlCards.ControlCards.Service.ArchivedCardService;
import com.ControlCards.ControlCards.Service.CardService;
import com.ControlCards.ControlCards.Service.LogEntryService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import com.ControlCards.ControlCards.Service.WorkshopService;
import com.ControlCards.ControlCards.Util.Enums.CardStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final ArchivedCardService archivedCardService;
    private final LogEntryService logEntryService;
    private final WorkshopService workshopService;
    private final WorkCenterService workCenterService;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository, ArchivedCardService archivedCardService, 
                          LogEntryService logEntryService, WorkshopService workshopService,
                          WorkCenterService workCenterService) {
        this.cardRepository = cardRepository;
        this.archivedCardService = archivedCardService;
        this.logEntryService = logEntryService;
        this.workshopService = workshopService;
        this.workCenterService = workCenterService;
    }

    @Override
    public void createCard(CardCreateDTO cardCreateDTO, User currentUser) {
        log.info("Creating new card for user: {}", currentUser.getUsername());
        
        Card card = new Card();
        card.setShortDescription(cardCreateDTO.getShortDescription());
        card.setDetailedDescription(cardCreateDTO.getDetailedDescription());
        card.setResolutionDurationMinutes(cardCreateDTO.getResolutionDurationMinutes());
        card.setShift(cardCreateDTO.getShift());
        card.setStatus(CardStatus.CREATED);
        card.setCreatedBy(currentUser);
        card.setCreatedAt(LocalDateTime.now());
        
        Workshop workshop = workshopService.findById(cardCreateDTO.getWorkshopId())
                .orElseThrow(() -> new RuntimeException("Workshop not found"));
        card.setWorkshop(workshop);
        
        WorkCenter workCenter = workCenterService.findById(cardCreateDTO.getWorkCenterId())
                .orElseThrow(() -> new RuntimeException("Work center not found"));
        card.setWorkCenter(workCenter);
        
        cardRepository.save(card);
        
        log.info("Card created successfully with ID: {}", card.getId());
        logEntryService.createLog(currentUser, "Created new card: " + card.getShortDescription());
    }

    @Override
    public List<CardViewDTO> getCreatedCards() {
        log.debug("Retrieving all created cards");
        List<Card> cards = cardRepository.findByStatus(CardStatus.CREATED);
        return cards.stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardViewDTO> getExtendedCards() {
        log.debug("Retrieving all extended cards");
        List<Card> cards = cardRepository.findByStatus(CardStatus.EXTENDED);
        return cards.stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void extendCard(UUID cardId, User currentUser) {
        log.info("Extending card ID: {} by user: {}", cardId, currentUser.getUsername());
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
        
        if (card.getStatus() != CardStatus.CREATED) {
            throw new InvalidCardStatusException("Card is not in CREATED status");
        }
        
        card.extend(currentUser);
        cardRepository.save(card);
        
        log.info("Card extended successfully");
        logEntryService.createLog(currentUser, "Extended card ID: " + cardId + " - " + card.getShortDescription());
    }

    @Override
    public void closeCard(UUID cardId, User currentUser) {
        log.info("Closing card ID: {} by user: {}", cardId, currentUser.getUsername());
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
        
        if (card.getStatus() != CardStatus.EXTENDED) {
            throw new InvalidCardStatusException("Card is not in EXTENDED status");
        }
        
        card.close(currentUser);
        cardRepository.save(card);
        
        ArchivedCard archivedCard = new ArchivedCard(card, currentUser);
        archivedCardService.save(archivedCard);
        
        cardRepository.deleteById(cardId);
        
        log.info("Card closed and archived successfully");
        logEntryService.createLog(currentUser, "Closed card ID: " + cardId + " - " + card.getShortDescription());
    }

    @Override
    public boolean cardExists(UUID cardId) {
        return cardRepository.existsById(cardId);
    }

    @Override
    public boolean canExtendCard(UUID cardId, User currentUser) {
        Card card = cardRepository.findById(cardId).orElse(null);
        if (card == null || card.getStatus() != CardStatus.CREATED) {
            return false;
        }
        return card.getExtendedBy() == null || !card.getExtendedBy().getId().equals(currentUser.getId());
    }

    @Override
    public boolean canCloseCard(UUID cardId) {
        Card card = cardRepository.findById(cardId).orElse(null);
        return card != null && card.getStatus() == CardStatus.EXTENDED;
    }

    private CardViewDTO convertToViewDTO(Card card) {
        CardViewDTO dto = new CardViewDTO();
        dto.setId(card.getId());
        dto.setWorkshopName(card.getWorkshop() != null ? card.getWorkshop().getName() : null);
        dto.setWorkCenterName(card.getWorkCenter() != null ? card.getWorkCenter().getNumber() : null);
        dto.setShift(card.getShift());
        dto.setShortDescription(card.getShortDescription());
        dto.setDetailedDescription(card.getDetailedDescription());
        dto.setResolutionDurationMinutes(card.getResolutionDurationMinutes());
        dto.setStatus(card.getStatus());
        
        if (card.getCreatedBy() != null) {
            dto.setCreatedByUsername(card.getCreatedBy().getUsername());
        }
        if (card.getUpdatedBy() != null) {
            dto.setUpdatedByUsername(card.getUpdatedBy().getUsername());
        }
        if (card.getExtendedBy() != null) {
            dto.setExtendedByUsername(card.getExtendedBy().getUsername());
        }
        if (card.getClosedBy() != null) {
            dto.setClosedByUsername(card.getClosedBy().getUsername());
        }
        
        dto.setCreatedAt(card.getCreatedAt());
        dto.setUpdatedAt(card.getUpdatedAt());
        dto.setExtendedAt(card.getExtendedAt());
        dto.setClosedAt(card.getClosedAt());
        
        return dto;
    }
}

