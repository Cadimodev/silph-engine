package com.silphengine.domain.dto.requests;

import com.silphengine.domain.enums.CardCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CardRequest (

        @NotBlank(message = "External id is mandatory")
        String externalId,

        @NotBlank(message = "Name is mandatory")
        String name,

        @NotBlank(message = "Expansion external id is mandatory")
        String expansionExternalId,

        @NotBlank(message = "Rarity is mandatory")
        String rarity,

        @NotBlank(message = "Card category is mandatory")
        String cardCategory,

        List<String> types,

        String imageUrl
){
}
