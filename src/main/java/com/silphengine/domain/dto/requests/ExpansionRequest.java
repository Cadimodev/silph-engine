package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ExpansionRequest(
        @NotBlank(message = "External id is mandatory")
        String externalId,

        @NotBlank(message = "Name is mandatory")
        String name,

        @NotBlank(message = "Serie name is mandatory")
        String serieName,

        @NotNull(message = "Release date is mandatory")
        LocalDate releaseDate,

        @Min(value = 1, message = "Expansion must have at least one card")
        int totalCards,

        String logoUrl
) {}