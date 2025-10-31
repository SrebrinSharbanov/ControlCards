package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.ArchivedCard;
import com.ControlCards.ControlCards.Repository.ArchivedCardRepository;
import com.ControlCards.ControlCards.Service.ArchivedCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArchivedCardServiceImpl implements ArchivedCardService {

    private final ArchivedCardRepository archivedCardRepository;

    @Autowired
    public ArchivedCardServiceImpl(ArchivedCardRepository archivedCardRepository) {
        this.archivedCardRepository = archivedCardRepository;
    }

    @Override
    public ArchivedCard save(ArchivedCard archivedCard) {
        return archivedCardRepository.save(archivedCard);
    }
}

