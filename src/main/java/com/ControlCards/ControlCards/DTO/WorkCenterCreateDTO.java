package com.ControlCards.ControlCards.DTO;

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
public class WorkCenterCreateDTO {
    
    @NotBlank(message = "Work center number is required")
    @Size(max = 5, message = "Work center number must not exceed 5 characters")
    private String number;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 100, message = "Machine type must not exceed 100 characters")
    private String machineType;
    
    @NotNull(message = "Workshop is required")
    private UUID workshopId;
}

