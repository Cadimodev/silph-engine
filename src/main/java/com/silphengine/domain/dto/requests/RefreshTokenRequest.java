package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token is mandatory")
        String refreshToken
){}