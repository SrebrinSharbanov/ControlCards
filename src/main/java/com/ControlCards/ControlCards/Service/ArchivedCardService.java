package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.ArchivedCard;
import com.ControlCards.ControlCards.Util.Enums.Shift;

import java.util.List;
import java.util.Optional;

public interface ArchivedCardService {
    // CRUD операции

    Optional<ArchivedCard> findById(Long id);
    ArchivedCard save(ArchivedCard archivedCard);
    boolean existsById(Long id);

    // Специфични методи
    List<ArchivedCard> findByWorkshopId(Long workshopId);
    List<ArchivedCard> findByWorkCenterId(Integer workCenterId);
    List<ArchivedCard> findByShift(Shift shift);
    List<ArchivedCard> findByShortDescriptionContaining(String shortDescription);
    List<ArchivedCard> findByDetailedDescriptionContaining(String detailedDescription);
    List<ArchivedCard> findByArchivedById(Long archivedById);
}
