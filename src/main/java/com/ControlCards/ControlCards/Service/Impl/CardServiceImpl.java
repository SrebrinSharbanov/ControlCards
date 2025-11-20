package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.DTO.CardCreateDTO;
import com.ControlCards.ControlCards.DTO.CardExtendDTO;
import com.ControlCards.ControlCards.DTO.CardViewDTO;
import com.ControlCards.ControlCards.Exception.CardNotFoundException;
import com.ControlCards.ControlCards.Exception.InvalidCardStatusException;
import com.ControlCards.ControlCards.Exception.WorkshopNotFoundException;
import com.ControlCards.ControlCards.Exception.WorkCenterNotFoundException;
import com.ControlCards.ControlCards.Model.ArchivedCard;
import com.ControlCards.ControlCards.Model.Card;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Repository.CardRepository;
import com.ControlCards.ControlCards.Service.ArchivedCardService;
import com.ControlCards.ControlCards.Service.CardService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import com.ControlCards.ControlCards.Service.WorkshopService;
import com.ControlCards.ControlCards.Util.Enums.CardStatus;
import com.ControlCards.ControlCards.Util.Enums.Role;
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
        card.setShift(cardCreateDTO.getShift());
        card.setStatus(CardStatus.CREATED);
        card.setCreatedBy(currentUser);
        card.setCreatedAt(LocalDateTime.now());
        
        Workshop workshop = workshopService.findById(cardCreateDTO.getWorkshopId())
                .orElseThrow(() -> new WorkshopNotFoundException("Workshop not found: " + cardCreateDTO.getWorkshopId()));
        card.setWorkshop(workshop);
        
        WorkCenter workCenter = workCenterService.findById(cardCreateDTO.getWorkCenterId())
                .orElseThrow(() -> new WorkCenterNotFoundException("Work center not found: " + cardCreateDTO.getWorkCenterId()));
        card.setWorkCenter(workCenter);
        
        cardRepository.save(card);
        
        log.info("Card created successfully with ID: {}", card.getId());
        logEntryService.createLog(currentUser, "Създадена нова карта: " + card.getShortDescription());
    }

    @Override
    public List<CardViewDTO> getCreatedCards(User currentUser) {
        log.debug("Retrieving created cards for user: {}", currentUser.getUsername());
        List<Card> cards;
        
        if (currentUser.getRole() == Role.ADMIN) {
            cards = cardRepository.findByStatus(CardStatus.CREATED);
        } else {
            List<Workshop> userWorkshops = currentUser.getWorkshops();
            if (userWorkshops == null || userWorkshops.isEmpty()) {
                log.debug("User {} has no workshops, returning empty list", currentUser.getUsername());
                return List.of();
            }
            cards = cardRepository.findByStatusAndWorkshopIn(CardStatus.CREATED, userWorkshops);
        }
        
        return cards.stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardViewDTO> getExtendedCards(User currentUser) {
        log.debug("Retrieving extended cards for user: {}", currentUser.getUsername());
        List<Card> cards;
        
        if (currentUser.getRole() == Role.ADMIN) {
            cards = cardRepository.findByStatus(CardStatus.EXTENDED);
        } else {
            List<Workshop> userWorkshops = currentUser.getWorkshops();
            if (userWorkshops == null || userWorkshops.isEmpty()) {
                log.debug("User {} has no workshops, returning empty list", currentUser.getUsername());
                return List.of();
            }
            cards = cardRepository.findByStatusAndWorkshopIn(CardStatus.EXTENDED, userWorkshops);
        }
        
        return cards.stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardViewDTO> getClosedCards(User currentUser) {
        log.debug("Retrieving closed cards for user: {}", currentUser.getUsername());
        List<Card> cards;
        
        if (currentUser.getRole() == Role.ADMIN) {
            cards = cardRepository.findByStatus(CardStatus.CLOSED);
        } else {
            List<Workshop> userWorkshops = currentUser.getWorkshops();
            if (userWorkshops == null || userWorkshops.isEmpty()) {
                log.debug("User {} has no workshops, returning empty list", currentUser.getUsername());
                return List.of();
            }
            cards = cardRepository.findByStatusAndWorkshopIn(CardStatus.CLOSED, userWorkshops);
        }
        
        return cards.stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardViewDTO> getAllCards(User currentUser) {
        log.debug("Retrieving all cards for user: {}", currentUser.getUsername());
        List<Card> cards;
        
        if (currentUser.getRole() == Role.ADMIN) {
            cards = cardRepository.findAll();
        } else {
            List<Workshop> userWorkshops = currentUser.getWorkshops();
            if (userWorkshops == null || userWorkshops.isEmpty()) {
                log.debug("User {} has no workshops, returning empty list", currentUser.getUsername());
                return List.of();
            }
            cards = cardRepository.findByWorkshopIn(userWorkshops);
        }
        
        return cards.stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void extendCard(UUID cardId, CardExtendDTO cardExtendDTO, User currentUser) {
        log.info("Extending card ID: {} by user: {}", cardId, currentUser.getUsername());
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
        
        if (card.getStatus() != CardStatus.CREATED) {
            throw new InvalidCardStatusException("Card is not in CREATED status");
        }
        
        if (cardExtendDTO.getDetailedDescription() != null && !cardExtendDTO.getDetailedDescription().trim().isEmpty()) {
            card.setDetailedDescription(cardExtendDTO.getDetailedDescription());
        }
        if (cardExtendDTO.getResolutionDurationMinutes() != null) {
            card.setResolutionDurationMinutes(cardExtendDTO.getResolutionDurationMinutes());
        }
        
        card.extend(currentUser);
        card.setUpdatedBy(currentUser);
        card.setUpdatedAt(LocalDateTime.now());
        cardRepository.save(card);
        
        log.info("Card extended successfully");
        logEntryService.createLog(currentUser, "Разширена карта ID: " + cardId + " - " + card.getShortDescription());
    }

    @Override
    public void closeCard(UUID cardId, User currentUser) {
        log.info("Closing card ID: {} by user: {}", cardId, currentUser.getUsername());
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
        
        if (currentUser.getRole() != Role.ADMIN) {
            if (card.getStatus() != CardStatus.EXTENDED) {
                throw new InvalidCardStatusException("Card is not in EXTENDED status");
            }
        } else {
            if (card.getStatus() != CardStatus.EXTENDED && card.getStatus() != CardStatus.CREATED) {
                throw new InvalidCardStatusException("Card is not in CREATED or EXTENDED status");
            }
        }
        
        card.close(currentUser);
        cardRepository.save(card);
        
        log.info("Card closed successfully");
        logEntryService.createLog(currentUser, "Затворена карта ID: " + cardId + " - " + card.getShortDescription());
    }

    @Override
    public List<CardViewDTO> getArchivedCards(User currentUser) {
        log.debug("Retrieving archived cards for user: {}", currentUser.getUsername());
        List<ArchivedCard> archivedCards = archivedCardService.findAll();
        
        if (currentUser.getRole() != Role.ADMIN) {
            List<Workshop> userWorkshops = currentUser.getWorkshops();
            if (userWorkshops == null || userWorkshops.isEmpty()) {
                log.debug("User {} has no workshops, returning empty list", currentUser.getUsername());
                return List.of();
            }
            List<UUID> workshopIds = userWorkshops.stream()
                    .map(Workshop::getId)
                    .toList();
            archivedCards = archivedCards.stream()
                    .filter(card -> card.getWorkshop() != null && workshopIds.contains(card.getWorkshop().getId()))
                    .toList();
        }
        
        return archivedCards.stream()
                .map(this::convertArchivedToViewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void archiveCard(UUID cardId, User currentUser) {
        log.info("Archiving card ID: {} by user: {}", cardId, currentUser.getUsername());
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
        
        if (card.getStatus() != CardStatus.CLOSED) {
            throw new InvalidCardStatusException("Card is not in CLOSED status");
        }
        
        ArchivedCard archivedCard = new ArchivedCard(card, currentUser);
        archivedCardService.save(archivedCard);
        
        cardRepository.deleteById(cardId);
        
        log.info("Card archived successfully");
        logEntryService.createLog(currentUser, "Архивирана карта ID: " + cardId + " - " + card.getShortDescription());
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

    @Override
    public boolean canArchiveCard(UUID cardId) {
        Card card = cardRepository.findById(cardId).orElse(null);
        return card != null && card.getStatus() == CardStatus.CLOSED;
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
            String createdByName = (card.getCreatedBy().getFirstName() != null ? card.getCreatedBy().getFirstName() : "") +
                    (card.getCreatedBy().getLastName() != null ? " " + card.getCreatedBy().getLastName() : "");
            dto.setCreatedByName(createdByName.trim().isEmpty() ? card.getCreatedBy().getUsername() : createdByName.trim());
        }
        if (card.getUpdatedBy() != null) {
            dto.setUpdatedByUsername(card.getUpdatedBy().getUsername());
        }
        if (card.getExtendedBy() != null) {
            dto.setExtendedByUsername(card.getExtendedBy().getUsername());
            String extendedByName = (card.getExtendedBy().getFirstName() != null ? card.getExtendedBy().getFirstName() : "") +
                    (card.getExtendedBy().getLastName() != null ? " " + card.getExtendedBy().getLastName() : "");
            dto.setExtendedByName(extendedByName.trim().isEmpty() ? card.getExtendedBy().getUsername() : extendedByName.trim());
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

    private CardViewDTO convertArchivedToViewDTO(ArchivedCard archivedCard) {
        CardViewDTO dto = new CardViewDTO();
        dto.setId(archivedCard.getId());
        dto.setWorkshopName(archivedCard.getWorkshop() != null ? archivedCard.getWorkshop().getName() : null);
        dto.setWorkCenterName(archivedCard.getWorkCenter() != null ? archivedCard.getWorkCenter().getNumber() : null);
        dto.setShift(archivedCard.getShift());
        dto.setShortDescription(archivedCard.getShortDescription());
        dto.setDetailedDescription(archivedCard.getDetailedDescription());
        dto.setResolutionDurationMinutes(archivedCard.getResolutionDurationMinutes());
        dto.setStatus(CardStatus.CLOSED);
        
        if (archivedCard.getCreatedBy() != null) {
            dto.setCreatedByUsername(archivedCard.getCreatedBy().getUsername());
            String createdByName = (archivedCard.getCreatedBy().getFirstName() != null ? archivedCard.getCreatedBy().getFirstName() : "") +
                    (archivedCard.getCreatedBy().getLastName() != null ? " " + archivedCard.getCreatedBy().getLastName() : "");
            dto.setCreatedByName(createdByName.trim().isEmpty() ? archivedCard.getCreatedBy().getUsername() : createdByName.trim());
        }
        if (archivedCard.getUpdatedBy() != null) {
            dto.setUpdatedByUsername(archivedCard.getUpdatedBy().getUsername());
        }
        if (archivedCard.getArchivedBy() != null) {
            dto.setExtendedByUsername(archivedCard.getArchivedBy().getUsername());
            String archivedByName = (archivedCard.getArchivedBy().getFirstName() != null ? archivedCard.getArchivedBy().getFirstName() : "") +
                    (archivedCard.getArchivedBy().getLastName() != null ? " " + archivedCard.getArchivedBy().getLastName() : "");
            dto.setExtendedByName(archivedByName.trim().isEmpty() ? archivedCard.getArchivedBy().getUsername() : archivedByName.trim());
        }
        
        dto.setCreatedAt(archivedCard.getCreatedAt());
        dto.setUpdatedAt(archivedCard.getUpdatedAt());
        dto.setExtendedAt(archivedCard.getUpdatedAt());
        dto.setClosedAt(archivedCard.getArchivedAt());
        
        return dto;
    }
}

