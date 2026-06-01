package com.silphengine.domain.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeckCardRequest(
        @NotNull(message = "Card id is mandatory")
        UUID cardId,

        @NotNull(message = "Quantity is mandatory")
        @Min(value = 0, message = "Quantity cannot be negative")
        Integer quantity
) {}
