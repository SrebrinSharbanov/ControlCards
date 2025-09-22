package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.ArchivedCard;
import com.ControlCards.ControlCards.Repository.ArchivedCardRepository;
import com.ControlCards.ControlCards.Service.ArchivedCardService;
import com.ControlCards.ControlCards.Util.Enums.Shift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArchivedCardServiceImpl implements ArchivedCardService {

    private final ArchivedCardRepository archivedCardRepository;

    @Autowired
    public ArchivedCardServiceImpl(ArchivedCardRepository archivedCardRepository) {
        this.archivedCardRepository = archivedCardRepository;
    }

    @Override
    public Optional<ArchivedCard> findById(Long id) {
        return archivedCardRepository.findById(id);
    }

    @Override
    public ArchivedCard save(ArchivedCard archivedCard) {
        return archivedCardRepository.save(archivedCard);
    }

    @Override
    public boolean existsById(Long id) {
        return archivedCardRepository.existsById(id);
    }

    @Override
    public List<ArchivedCard> findByWorkshopId(Long workshopId) {
        return archivedCardRepository.findByWorkshopId(workshopId);
    }

    @Override
    public List<ArchivedCard> findByWorkCenterId(Integer workCenterId) {
        return archivedCardRepository.findByWorkCenterId(workCenterId);
    }

    @Override
    public List<ArchivedCard> findByShift(Shift shift) {
        return archivedCardRepository.findByShift(shift);
    }

    @Override
    public List<ArchivedCard> findByShortDescriptionContaining(String shortDescription) {
        return archivedCardRepository.findByShortDescriptionContainingIgnoreCase(shortDescription);
    }

    @Override
    public List<ArchivedCard> findByDetailedDescriptionContaining(String detailedDescription) {
        return archivedCardRepository.findByDetailedDescriptionContainingIgnoreCase(detailedDescription);
    }

    @Override
    public List<ArchivedCard> findByArchivedById(Long archivedById) {
        return archivedCardRepository.findByArchivedById(archivedById);
    }
}
