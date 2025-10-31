package com.ControlCards.ControlCards.DTO;

import com.ControlCards.ControlCards.Util.Enums.Shift;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateDTO {
    
    @NotNull(message = "Workshop is required")
    private UUID workshopId;
    
    @NotNull(message = "Work center is required")
    private UUID workCenterId;
    
    @NotNull(message = "Shift is required")
    private Shift shift;
    
    @NotBlank(message = "Short description is required")
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;
    
    @Size(max = 2000, message = "Detailed description must not exceed 2000 characters")
    private String detailedDescription;
    
    private Integer resolutionDurationMinutes;
}

