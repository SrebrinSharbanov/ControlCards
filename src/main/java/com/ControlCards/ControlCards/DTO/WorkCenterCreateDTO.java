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
    
    @NotBlank(message = "Номерът на работния център е задължителен")
    @Size(max = 5, message = "Номерът на работния център не може да бъде повече от 5 символа")
    private String number;
    
    @Size(max = 500, message = "Описанието не може да бъде повече от 500 символа")
    private String description;
    
    @Size(max = 100, message = "Типът на машината не може да бъде повече от 100 символа")
    private String machineType;
    
    @NotNull(message = "Цехът е задължителен")
    private UUID workshopId;
}

