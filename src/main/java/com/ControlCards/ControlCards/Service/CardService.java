package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.DTO.CardCreateDTO;
import com.ControlCards.ControlCards.DTO.CardViewDTO;
import com.ControlCards.ControlCards.Model.User;

import java.util.List;
import java.util.UUID;

public interface CardService {

    void createCard(CardCreateDTO cardCreateDTO, User currentUser);
    List<CardViewDTO> getCreatedCards();
    List<CardViewDTO> getExtendedCards();
    List<CardViewDTO> getAllCards();
    void extendCard(UUID cardId, User currentUser);
    void closeCard(UUID cardId, User currentUser);
    
    boolean cardExists(UUID cardId);
    boolean canExtendCard(UUID cardId, User currentUser);
    boolean canCloseCard(UUID cardId);
}

