package com.ControlCards.ControlCards.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateDTO {
    
    @NotBlank(message = "Името е задължително")
    @Size(max = 50, message = "Името не може да бъде повече от 50 символа")
    private String firstName;
    
    @NotBlank(message = "Фамилията е задължителна")
    @Size(max = 50, message = "Фамилията не може да бъде повече от 50 символа")
    private String lastName;
    
    @Size(max = 255, message = "Паролата не може да бъде повече от 255 символа")
    private String password;
}

