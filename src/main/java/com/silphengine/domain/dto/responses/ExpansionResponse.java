package com.silphengine.domain.dto.responses;

import java.time.LocalDate;

public record ExpansionResponse(
        String externalId,
        String name,
        String serieName,
        LocalDate releaseDate,
        int totalCards,
        String logoUrl
) {}