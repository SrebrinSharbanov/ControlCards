package com.ControlCards.ControlCards.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopCreateDTO {
    
    @NotBlank(message = "Името на цеха е задължително")
    @Size(max = 100, message = "Името на цеха не може да бъде повече от 100 символа")
    private String name;
    
    @Size(max = 500, message = "Описанието не може да бъде повече от 500 символа")
    private String description;
}

