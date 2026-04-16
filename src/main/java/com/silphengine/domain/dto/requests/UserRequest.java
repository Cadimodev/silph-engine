package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(

        @NotBlank(message = "Nickname is mandatory")
        @Size(min = 3, max = 20, message = "Nickname must be between 3 and 20 characters")
        String nickname,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email must be a valid email format")
        String email,

        @NotBlank(message = "Password is mandatory")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¡?¿])(?=\\S+$).{8,}$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, one special character, and no whitespaces"
        )
        String password
){}
