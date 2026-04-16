package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserProfileRequest(
        @NotBlank(message = "Nickname is mandatory")
        String nickname,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid")
        String email
) {}
