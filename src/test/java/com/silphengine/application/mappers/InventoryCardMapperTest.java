package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.UpdateInventoryCardRequest;
import com.silphengine.domain.entities.InventoryCard;
import com.silphengine.domain.enums.CardCondition;
import com.silphengine.domain.enums.CardFinish;
import com.silphengine.domain.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InventoryCardMapperTest {

    private InventoryCardMapper inventoryCardMapper;

    @BeforeEach
    void setUp() {
        inventoryCardMapper = new InventoryCardMapperImpl();
    }

    @Test
    void mapStringToCardCondition_shouldReturnEnum_whenStringIsValid() {

        // Given
        String validCategory = " near mint ";

        // When
        CardCondition result = inventoryCardMapper.mapStringToCardCondition(validCategory);

        // Then
        assertEquals(CardCondition.NEAR_MINT, result);
    }

    @Test
    void mapStringToCardCondition_shouldThrowBadRequestException_whenStringIsNullOrBlank() {

        // Given
        String nullCardCondition = null;
        String blankCardCondition = "   ";

        // When & Then
        assertThrows(BadRequestException.class, () -> inventoryCardMapper.mapStringToCardCondition(nullCardCondition));
        assertThrows(BadRequestException.class, () -> inventoryCardMapper.mapStringToCardCondition(blankCardCondition));
    }

    @Test
    void mapStringToCardFinish_shouldReturnEnum_whenStringIsValid() {

        // Given
        String validCategory = " reverse holo ";

        // When
        CardFinish result = inventoryCardMapper.mapStringToCardFinish(validCategory);

        // Then
        assertEquals(CardFinish.REVERSE_HOLO, result);
    }

    @Test
    void mapStringToCardFinish_shouldThrowBadRequestException_whenStringIsNullOrBlank() {

        // Given
        String nullCardFinish = null;
        String blankCardFinish = "   ";

        // When & Then
        assertThrows(BadRequestException.class, () -> inventoryCardMapper.mapStringToCardFinish(nullCardFinish));
        assertThrows(BadRequestException.class, () -> inventoryCardMapper.mapStringToCardFinish(blankCardFinish));
    }

    @Test
    void updateEntityFromRequest_shouldUpdateCardProperly_whenRequestIsValid() {

        // Given
        InventoryCard existingCard = InventoryCard.builder()
                .quantity(1)
                .build();


        UpdateInventoryCardRequest updateRequest = new UpdateInventoryCardRequest(2);

        // When
        inventoryCardMapper.updateEntityFromRequest(existingCard, updateRequest);

        // Then
        assertEquals(2, existingCard.getQuantity());
    }

    @Test
    void updateEntityFromRequest_shouldDoNothing_whenRequestOrCardIsNull() {

        // Given
        InventoryCard existingCard = InventoryCard.builder()
                .quantity(1)
                .build();

        // When
        inventoryCardMapper.updateEntityFromRequest(existingCard, null);
        inventoryCardMapper.updateEntityFromRequest(null, new UpdateInventoryCardRequest(2));

        // Then
        assertEquals(1, existingCard.getQuantity());
    }

}
