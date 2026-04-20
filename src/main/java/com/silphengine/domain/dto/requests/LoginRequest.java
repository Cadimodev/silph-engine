package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Nickname is mandatory")
        String nickname,

        @NotBlank(message = "Password is mandatory")
        String password
){}