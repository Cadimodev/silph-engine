package com.silphengine.domain.dto.responses;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String nickname
) {}
