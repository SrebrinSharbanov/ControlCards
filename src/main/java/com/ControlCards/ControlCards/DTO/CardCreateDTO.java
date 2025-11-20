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
    
    @NotNull(message = "Цехът е задължителен")
    private UUID workshopId;
    
    @NotNull(message = "Работният център е задължителен")
    private UUID workCenterId;
    
    @NotNull(message = "Смяната е задължителна")
    private Shift shift;
    
    @NotBlank(message = "Краткото описание е задължително")
    @Size(max = 500, message = "Краткото описание не трябва да надвишава 500 символа")
    private String shortDescription;
}

