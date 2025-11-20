package com.ControlCards.ControlCards.DTO;

import com.ControlCards.ControlCards.Util.Enums.CardStatus;
import com.ControlCards.ControlCards.Util.Enums.Shift;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CardViewDTO {
    
    private UUID id;
    private String workshopName;
    private String workCenterName;
    private Shift shift;
    private String shortDescription;
    private String detailedDescription;
    private Integer resolutionDurationMinutes;
    private CardStatus status;
    private String createdByUsername;
    private String createdByName; // Име и фамилия
    private String updatedByUsername;
    private String extendedByUsername;
    private String extendedByName; // Име и фамилия
    private String closedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime extendedAt;
    private LocalDateTime closedAt;
}

