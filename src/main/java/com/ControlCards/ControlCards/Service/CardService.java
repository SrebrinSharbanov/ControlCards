package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.DTO.CardCreateDTO;
import com.ControlCards.ControlCards.DTO.CardExtendDTO;
import com.ControlCards.ControlCards.DTO.CardViewDTO;
import com.ControlCards.ControlCards.Model.User;

import java.util.List;
import java.util.UUID;

public interface CardService {

    void createCard(CardCreateDTO cardCreateDTO, User currentUser);
    List<CardViewDTO> getCreatedCards(User currentUser);
    List<CardViewDTO> getExtendedCards(User currentUser);
    List<CardViewDTO> getClosedCards(User currentUser);
    List<CardViewDTO> getArchivedCards(User currentUser);
    List<CardViewDTO> getAllCards(User currentUser);
    void extendCard(UUID cardId, CardExtendDTO cardExtendDTO, User currentUser);
    void closeCard(UUID cardId, User currentUser);
    void archiveCard(UUID cardId, User currentUser);
    
    boolean cardExists(UUID cardId);
    boolean canExtendCard(UUID cardId, User currentUser);
    boolean canCloseCard(UUID cardId);
    boolean canArchiveCard(UUID cardId);
}

