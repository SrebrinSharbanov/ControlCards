package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.ArchivedCard;
import com.ControlCards.ControlCards.Util.Enums.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivedCardRepository extends JpaRepository<ArchivedCard, Long> {
    @Query("SELECT a FROM ArchivedCard a WHERE a.workshop.id = :workshopId")
    List<ArchivedCard> findByWorkshopId(@Param("workshopId") Long workshopId);

    @Query("SELECT a FROM ArchivedCard a WHERE a.workCenter.id = :workCenterId")
    List<ArchivedCard> findByWorkCenterId(@Param("workCenterId") Integer workCenterId);

    List<ArchivedCard> findByShift(Shift shift);
    List<ArchivedCard> findByShortDescriptionContainingIgnoreCase(String shortDescription);
    List<ArchivedCard> findByDetailedDescriptionContainingIgnoreCase(String detailedDescription);

    @Query("SELECT a FROM ArchivedCard a WHERE a.archivedBy.id = :archivedById")
    List<ArchivedCard> findByArchivedById(@Param("archivedById") Long archivedById);
}
