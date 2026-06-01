package com.silphengine.application.services;

import com.silphengine.application.config.FormatProperties;
import com.silphengine.application.mappers.DeckMapper;
import com.silphengine.domain.dto.requests.DeckCardRequest;
import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.DeckResponse;
import com.silphengine.domain.entities.*;
import com.silphengine.domain.enums.CardCategory;
import com.silphengine.domain.enums.CardType;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.DeckService;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.DeckRepository;
import com.silphengine.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeckServiceImplTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckMapper deckMapper;

    @Mock
    private FormatProperties formatProperties;

    private DeckService deckService;

    private Deck deck;
    private DeckResponse deckResponse;
    private User owner;
    private Card card;
    private DeckRequest deckRequest;
    private DeckCard deckCard;
    private Expansion expansion;

    @BeforeEach
    void setUp() {

        deckService = new DeckServiceImpl(deckMapper, deckRepository, userRepository, cardRepository, formatProperties);

        owner = User.builder()
                .id(UUID.randomUUID())
                .nickname("Ash")
                .build();
                
        expansion = Expansion.builder()
                .id(UUID.randomUUID())
                .externalId("sv02")
                .name("Paldea Evolved")
                .build();

        card = Card.builder()
                .id(UUID.randomUUID())
                .externalId("sv02-203")
                .name("Magikarp")
                .rarity("Illustration rare")
                .cardCategory(CardCategory.POKEMON)
                .types(List.of(CardType.WATER))
                .imageUrl("https://assets.tcgdex.net/en/sv/sv02/203")
                .regulationMark("G")
                .expansion(expansion)
                .build();

        deckCard = DeckCard.builder()
                .card(card)
                .quantity(4)
                .build();

        deckRequest = new DeckRequest("Test Deck", List.of(new DeckCardRequest(card.getId(), 4)));

        deck = Deck.builder()
                .id(UUID.randomUUID())
                .name(deckRequest.name())
                .owner(owner)
                .isLegal(false)
                .cards(new ArrayList<>(List.of(deckCard)))
                .build();

        deckResponse = new DeckResponse(
                deck.getId(),
                owner.getId(),
                deck.getName(),
                deck.getIsLegal(),
                List.of()
        );
    }

    @Test
    void createDeck_shouldReturnDeckResponse_whenDeckIsCreatedSuccessfully() {

        // Given
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.empty());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(owner));
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.of(card));

        when(deckMapper.toEntity(eq(deckRequest), eq(owner), anyList())).thenReturn(deck);
        when(deckMapper.toResponse(eq(deck))).thenReturn(deckResponse);

        when(formatProperties.getStandardValidMarks()).thenReturn(List.of("G", "H", "I"));

        when(deckRepository.save(any(Deck.class))).thenReturn(deck);

        // When
        DeckResponse result = deckService.createDeck(deckRequest, owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(deckResponse.name(), result.name());
        assertEquals(deckResponse.id(), result.id());

        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
        verify(userRepository, times(1)).findById(owner.getId());
        verify(cardRepository, times(1)).findById(card.getId());
        verify(deckMapper, times(1)).toEntity(eq(deckRequest), eq(owner), anyList());
        verify(deckRepository, times(1)).save(deck);
        verify(deckMapper, times(1)).toResponse(deck);
        verify(formatProperties, times(1)).getStandardValidMarks();
    }

    @Test
    void createDeck_shouldThrowDuplicateResource_whenDeckAlreadyExists() {

        // Given
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.of(deck));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> deckService.createDeck(deckRequest, owner.getId()));
        assertEquals("Deck already exists with that name for this user", exception.getMessage());

        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
    }

    @Test
    void createDeck_shouldThrowResourceNotFound_whenUserDoesNotExists() {

        // Given
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.empty());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.createDeck(deckRequest, owner.getId()));
        assertEquals("User with ID: " + owner.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
        verify(userRepository, times(1)).findById(owner.getId());
    }

    @Test
    void createDeck_shouldThrowResourceNotFound_whenCardDoesNotExists() {

        // Given
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.empty());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(owner));
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.createDeck(deckRequest, owner.getId()));
        assertEquals("Card with ID: " + card.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
        verify(userRepository, times(1)).findById(owner.getId());
        verify(cardRepository, times(1)).findById(card.getId());
    }

    @Test
    void getByIdAndOwnerID_shouldReturnDeckResponse_whenDeckExists() {

        // Given
        when(deckRepository.findByIdAndOwnerId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(deck));
        when(deckMapper.toResponse(eq(deck))).thenReturn(deckResponse);

        // When
        DeckResponse result = deckService.getByIdAndOwnerID(deck.getId(), owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(deckResponse.name(), result.name());
        assertEquals(deckResponse.id(), result.id());
        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
    }

    @Test
    void getByIdAndOwnerID_shouldThrowResourceNotFound_whenDeckDoesNotExists() {

        // Given
        when(deckRepository.findByIdAndOwnerId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.getByIdAndOwnerID(deck.getId(), owner.getId()));
        assertEquals("Deck with ID: " + deck.getId() + " not found" , exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
    }

    @Test
    void getByOwnerId_shouldReturnPageOfDeckResponse() {

        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Deck> deckPage = new PageImpl<>(List.of(deck), pageable, 1);
        
        when(deckRepository.findByOwnerId(eq(owner.getId()), eq(pageable))).thenReturn(deckPage);
        when(deckMapper.toResponse(eq(deck))).thenReturn(deckResponse);

        // When
        Page<DeckResponse> result = deckService.getByOwnerId(owner.getId(), pageable);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(deckResponse.name(), result.getContent().getFirst().name());
        
        verify(deckRepository, times(1)).findByOwnerId(owner.getId(), pageable);
    }

    @Test
    void getByOwnerIdAndDeckName_shouldReturnPageWithDeckResponse_whenDeckExists() {

        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.of(deck));
        when(deckMapper.toResponse(eq(deck))).thenReturn(deckResponse);

        // When
        Page<DeckResponse> result = deckService.getByOwnerIdAndDeckName(owner.getId(), deckRequest.name(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(deckResponse.name(), result.getContent().getFirst().name());
        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
    }
    
    @Test
    void getByOwnerIdAndDeckName_shouldReturnEmptyPage_whenDeckDoesNotExists() {

        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(deckRepository.findByOwnerIdAndName(any(UUID.class), eq(deckRequest.name()))).thenReturn(Optional.empty());

        // When
        Page<DeckResponse> result = deckService.getByOwnerIdAndDeckName(owner.getId(), deckRequest.name(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEmpty());
        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), deckRequest.name());
    }

    @Test
    void updateDeck_shouldReturnDeckResponse_whenDeckExists() {

        // Given
        DeckRequest updateRequest = new DeckRequest("Update Test Deck", List.of(new DeckCardRequest(card.getId(), 4)));

        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));
        when(deckRepository.findByOwnerIdAndName(owner.getId(), updateRequest.name())).thenReturn(Optional.empty());
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.of(card));
        when(formatProperties.getStandardValidMarks()).thenReturn(List.of("G", "H", "I"));

        doNothing().when(deckMapper).updateEntityFromRequest(any(Deck.class), any(DeckRequest.class), anyList());

        // Must return the same object it receives
        when(deckRepository.save(any(Deck.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(deckMapper.toResponse(any(Deck.class))).thenReturn(
                new DeckResponse(deck.getId(), owner.getId(), updateRequest.name(), false, List.of())
        );

        // When
        DeckResponse result = deckService.updateDeck(deck.getId(), updateRequest, owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.name(), result.name());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), updateRequest.name());
        verify(deckRepository, times(1)).save(deck);
    }

    @Test
    void updateDeck_shouldThrowResourceNotFound_whenDeckDoesNotExists() {

        // Given
        DeckRequest updateRequest = new DeckRequest("Update Test Deck", List.of(new DeckCardRequest(card.getId(), 4)));

        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.updateDeck(deck.getId(), updateRequest, owner.getId()));

        assertEquals("Deck with ID: " + deck.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
    }

    @Test
    void updateDeck_shouldThrowDuplicateResource_whenDeckAlreadyExistsWithNewName() {

        // Given
        DeckRequest updateRequest = new DeckRequest("Existing Deck Name", List.of(new DeckCardRequest(card.getId(), 4)));

        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));
        when(deckRepository.findByOwnerIdAndName(owner.getId(), updateRequest.name())).thenReturn(Optional.of(deck));

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> deckService.updateDeck(deck.getId(), updateRequest, owner.getId()));

        assertEquals("Deck already exists with the name: " + updateRequest.name(), exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), updateRequest.name());
    }


    @Test
    void updateDeck_shouldThrowResourceNotFound_whenCardDoesNotExists() {

        // Given
        DeckRequest updateRequest = new DeckRequest("Existing Deck Name", List.of(new DeckCardRequest(card.getId(), 4)));

        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));
        when(deckRepository.findByOwnerIdAndName(owner.getId(), updateRequest.name())).thenReturn(Optional.empty());
        when(cardRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.updateDeck(deck.getId(), updateRequest, owner.getId()));

        assertEquals("Card with ID: " + card.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verify(deckRepository, times(1)).findByOwnerIdAndName(owner.getId(), updateRequest.name());
        verify(cardRepository, times(1)).findById(card.getId());
    }

    @Test
    void deleteDeck_shouldDeleteDeck_whenDeckExists() {

        // Given
        when(deckRepository.findByIdAndOwnerId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(deck));
        doNothing().when(deckRepository).delete(deck);

        // When
        deckService.deleteDeck(deck.getId(), owner.getId());

        // Then
        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verify(deckRepository, times(1)).delete(deck);
    }

    @Test
    void deleteDeck_shouldThrowResourceNotFoundException_whenDeckDoesNotExists() {

        // Given
        when(deckRepository.findByIdAndOwnerId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deckService.deleteDeck(deck.getId(), owner.getId()));
        assertEquals("Deck with ID: " + deck.getId() + " not found", exception.getMessage());

        verify(deckRepository, times(1)).findByIdAndOwnerId(deck.getId(), owner.getId());
        verifyNoMoreInteractions(deckRepository);
    }

    @Test
    void createEmptyDeck_shouldReturnDeckResponse_whenUserExists() {

        // Given
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(deckRepository.save(any(Deck.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deckMapper.toResponse(any(Deck.class))).thenAnswer(invocation -> {
            Deck savedDeck = invocation.getArgument(0);
            return new DeckResponse(savedDeck.getId(), savedDeck.getOwner().getId(), savedDeck.getName(), savedDeck.getIsLegal(), List.of());
        });

        // When
        DeckResponse result = deckService.createEmptyDeck(owner.getId());

        // Then
        assertNotNull(result);
        assertEquals("New Deck", result.name());
        verify(userRepository, times(1)).findById(owner.getId());
        verify(deckRepository, times(1)).save(any(Deck.class));
    }

    @Test
    void createEmptyDeck_shouldThrowResourceNotFound_whenUserDoesNotExists() {

        // Given
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> deckService.createEmptyDeck(owner.getId()));
        verify(userRepository, times(1)).findById(owner.getId());
    }

    @Test
    void updateDeckCardQuantity_shouldUpdateQuantity_whenCardExistsInDeck() {

        // Given
        DeckCardRequest request = new DeckCardRequest(card.getId(), 2);
        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        // When
        deckService.updateDeckCardQuantity(deck.getId(), owner.getId(), request);

        // Then
        assertEquals(2, deckCard.getQuantity());
        verify(deckRepository, times(1)).save(deck);
    }

    @Test
    void updateDeckCardQuantity_shouldAddCard_whenCardDoesNotExistsInDeck() {

        // Given
        Card newCard = Card.builder().id(UUID.randomUUID()).expansion(expansion).build();
        DeckCardRequest request = new DeckCardRequest(newCard.getId(), 1);
        deck.getCards().clear();

        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));
        when(cardRepository.findById(newCard.getId())).thenReturn(Optional.of(newCard));

        // When
        deckService.updateDeckCardQuantity(deck.getId(), owner.getId(), request);

        // Then
        assertEquals(1, deck.getCards().size());
        verify(deckRepository, times(1)).save(deck);
    }

    @Test
    void updateDeckCardQuantity_shouldRemoveCard_whenQuantityIsZeroOrLess() {

        // Given
        DeckCardRequest request = new DeckCardRequest(card.getId(), 0);
        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        // When
        deckService.updateDeckCardQuantity(deck.getId(), owner.getId(), request);

        // Then
        assertTrue(deck.getCards().isEmpty());
        verify(deckRepository, times(1)).save(deck);
    }

    @Test
    void updateDeckName_shouldUpdateName_whenNameIsValid() {

        // Given
        String newName = "Updated Deck Name";
        when(deckRepository.findByIdAndOwnerId(deck.getId(), owner.getId())).thenReturn(Optional.of(deck));

        // When
        deckService.updateDeckName(deck.getId(), owner.getId(), newName);

        // Then
        assertEquals(newName, deck.getName());
        verify(deckRepository, times(1)).save(deck);
    }

    @Test
    void updateDeckName_shouldThrowIllegalArgumentException_whenNameIsBlank() {

        // Given
        String newName = " ";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> deckService.updateDeckName(deck.getId(), owner.getId(), newName));

        verifyNoMoreInteractions(deckRepository);
    }
}