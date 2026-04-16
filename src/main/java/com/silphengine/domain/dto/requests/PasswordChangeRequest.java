package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank(message = "Old password is mandatory")
        String oldPassword,

        @NotBlank(message = "New password is mandatory")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        String newPassword
) {}
