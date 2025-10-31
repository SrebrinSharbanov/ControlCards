package com.ControlCards.ControlCards.Model;

import com.ControlCards.ControlCards.Util.Enums.Shift;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "archived_cards")
@Getter
@Setter
@ToString
public class ArchivedCard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    private User updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id", nullable = false)
    private Workshop workshop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_center_id", nullable = false)
    private WorkCenter workCenter;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false)
    private Shift shift;

    @Column(name = "short_description", nullable = false, length = 500)
    private String shortDescription;

    @Column(name = "detailed_description", length = 2000)
    private String detailedDescription;

    @Column(name = "resolution_duration_minutes")
    private Integer resolutionDurationMinutes;

    @Column(name = "archived_at", nullable = false, updatable = false)
    private LocalDateTime archivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archived_by_user_id", nullable = false)
    private User archivedBy;

    // Constructors
    public ArchivedCard() {
        this.archivedAt = LocalDateTime.now();
    }

    public ArchivedCard(Card card, User archivedBy) {
        this();
        this.createdBy = card.getCreatedBy();
        this.updatedBy = card.getUpdatedBy();
        this.createdAt = card.getCreatedAt();
        this.updatedAt = card.getUpdatedAt();
        this.workshop = card.getWorkshop();
        this.workCenter = card.getWorkCenter();
        this.shift = card.getShift();
        this.shortDescription = card.getShortDescription();
        this.detailedDescription = card.getDetailedDescription();
        this.resolutionDurationMinutes = card.getResolutionDurationMinutes();
        this.archivedBy = archivedBy;
    }
}

