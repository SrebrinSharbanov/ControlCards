package com.ControlCards.ControlCards.DTO;

import com.ControlCards.ControlCards.Util.Enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    
    @NotBlank(message = "Потребителското име е задължително")
    @Size(min = 3, max = 50, message = "Потребителското име трябва да бъде между 3 и 50 символа")
    private String username;
    
    @NotBlank(message = "Паролата е задължителна")
    @Size(min = 6, message = "Паролата трябва да бъде поне 6 символа")
    private String password;
    
    @NotBlank(message = "Името е задължително")
    @Size(max = 50, message = "Името не може да бъде повече от 50 символа")
    private String firstName;
    
    @NotBlank(message = "Фамилията е задължителна")
    @Size(max = 50, message = "Фамилията не може да бъде повече от 50 символа")
    private String lastName;
    
    private Role role;
    private List<UUID> workshopIds;
}

