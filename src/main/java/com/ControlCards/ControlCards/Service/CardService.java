package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.User;

public interface CardService {

    void archiveCard(Long cardId, User archivedBy);

}
