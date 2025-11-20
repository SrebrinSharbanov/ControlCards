package com.ControlCards.ControlCards.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "Потребителското име е задължително")
    private String username;

    @NotBlank(message = "Паролата е задължителна")
    private String password;
}
