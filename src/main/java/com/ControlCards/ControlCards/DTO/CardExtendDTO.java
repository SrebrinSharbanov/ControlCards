package com.ControlCards.ControlCards.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardExtendDTO {
    
    @NotBlank(message = "Подробното описание е задължително")
    @Size(max = 2000, message = "Подробното описание не трябва да надвишава 2000 символа")
    private String detailedDescription;
    
    @NotNull(message = "Продължителността на разрешаването е задължителна")
    @Min(value = 1, message = "Продължителността трябва да е поне 1 минута")
    private Integer resolutionDurationMinutes;
}

