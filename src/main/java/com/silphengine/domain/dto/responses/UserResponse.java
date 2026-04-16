package com.silphengine.domain.dto.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String nickname,
        String email,
        LocalDateTime created
){}
