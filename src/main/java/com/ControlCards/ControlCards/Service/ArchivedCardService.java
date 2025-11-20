package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.ArchivedCard;

import java.util.List;

public interface ArchivedCardService {
    ArchivedCard save(ArchivedCard archivedCard);
    List<ArchivedCard> findAll();
}

