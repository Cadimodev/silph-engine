package com.silphengine.application.services;

import com.silphengine.application.config.FormatProperties;
import com.silphengine.application.mappers.DeckMapper;
import com.silphengine.domain.dto.requests.DeckCardRequest;
import com.silphengine.domain.dto.requests.DeckRequest;
import com.silphengine.domain.dto.responses.CardResponse;
import com.silphengine.domain.dto.responses.DeckCardResponse;
import com.silphengine.domain.dto.responses.DeckResponse;
import com.silphengine.domain.entities.Card;
import com.silphengine.domain.entities.Deck;
import com.silphengine.domain.entities.DeckCard;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.exceptions.DuplicateResourceException;
import com.silphengine.domain.exceptions.ResourceNotFoundException;
import com.silphengine.domain.services.DeckService;
import com.silphengine.infrastructure.repositories.CardRepository;
import com.silphengine.infrastructure.repositories.DeckRepository;
import com.silphengine.infrastructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeckServiceImpl implements DeckService {

    private final DeckMapper deckMapper;

    private final DeckRepository deckRepository;

    private final UserRepository userRepository;

    private final CardRepository cardRepository;

    private final FormatProperties formatProperties;

    @Override
    @Transactional
    public DeckResponse createDeck(DeckRequest deckRequest, UUID ownerId) {

        deckRepository.findByOwnerIdAndName(ownerId, deckRequest.name()).ifPresent(
                d -> { throw new DuplicateResourceException("Deck already exists with that name for this user"); }
        );
        
        User user = userRepository.findById(ownerId).orElseThrow(
                () -> new ResourceNotFoundException("User with ID: " + ownerId + " not found")
        );

        List<DeckCard> deckCards = getDeckCardFromRequest(deckRequest.cards());
        
        Deck newDeck = deckMapper.toEntity(deckRequest, user, deckCards);
        
        newDeck.evaluateLegality(formatProperties.getStandardValidMarks());
        
        return deckMapper.toResponse(deckRepository.save(newDeck));
    }

    @Override
    public DeckResponse getByIdAndOwnerID(UUID deckId, UUID ownerId) {

        Deck deck = deckRepository.findByIdAndOwnerId(deckId, ownerId).orElseThrow(
                () -> new ResourceNotFoundException("Deck with ID: " + deckId + " not found")
        );

        return deckMapper.toResponse(deck);
    }

    @Override
    public Page<DeckResponse> getByOwnerId(UUID ownerId, Pageable pageable) {

         Page<Deck> deckPage = deckRepository.findByOwnerId(ownerId, pageable);

         return deckPage.map(deckMapper::toResponse);
    }

    @Override
    public Page<DeckResponse> getByOwnerIdAndDeckName(UUID ownerId, String deckName, Pageable pageable) {

        Optional<Deck> deck = deckRepository.findByOwnerIdAndName(ownerId, deckName);

        if (deck.isEmpty()) {
            return Page.empty(pageable);
        }

        DeckResponse response = deckMapper.toResponse(deck.get());
        List<DeckResponse> content = List.of(response);

        return new PageImpl<>(content, pageable, 1);
    }

    @Override
    @Transactional
    public DeckResponse updateDeck(UUID deckId, DeckRequest deckRequest, UUID ownerId) {

        Deck deck = deckRepository.findByIdAndOwnerId(deckId, ownerId).orElseThrow(
                () -> new ResourceNotFoundException("Deck with ID: " + deckId + " not found")
        );

        if (!deck.getName().equalsIgnoreCase(deckRequest.name())) {
            deckRepository.findByOwnerIdAndName(ownerId, deckRequest.name()).ifPresent(
                    d -> { throw new DuplicateResourceException("Deck already exists with the name: " + deckRequest.name()); }
            );
        }

        List<DeckCard> deckCards = getDeckCardFromRequest(deckRequest.cards());

        deckMapper.updateEntityFromRequest(deck, deckRequest, deckCards);

        deck.evaluateLegality(formatProperties.getStandardValidMarks());

        return deckMapper.toResponse(deckRepository.save(deck));
    }

    @Override
    @Transactional
    public void deleteDeck(UUID deckId, UUID ownerId) {

        Deck deck = deckRepository.findByIdAndOwnerId(deckId, ownerId).orElseThrow(
                () -> new ResourceNotFoundException("Deck with ID: " + deckId + " not found")
        );

        deckRepository.delete(deck);
    }

    @Override
    @Transactional
    public DeckResponse createEmptyDeck(UUID ownerId) {

        User user = userRepository.findById(ownerId).orElseThrow(
                () -> new ResourceNotFoundException("User with ID: " + ownerId + " not found")
        );

        Deck newDeck = Deck.builder()
                .name("New Deck")
                .owner(user)
                .build();

        newDeck.evaluateLegality(formatProperties.getStandardValidMarks());

        return deckMapper.toResponse(deckRepository.save(newDeck));
    }

    @Override
    @Transactional
    public DeckCardResponse updateDeckCardQuantity(UUID deckId, UUID ownerId, DeckCardRequest request) {

        Deck deck = deckRepository.findByIdAndOwnerId(deckId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck with ID: " + deckId + " not found"));

        Card card = cardRepository.findById(request.cardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card with ID: " + request.cardId() + " not found"));

        Optional<DeckCard> existingLine = deck.getCards().stream()
                .filter(dc -> dc.getCard().getId().equals(card.getId()))
                .findFirst();

        int finalQuantity = 0;

        if (existingLine.isPresent()) {
            DeckCard line = existingLine.get();
            if (request.quantity() <= 0) {
                deck.removeCard(line);
            } else {
                line.changeQuantity(request.quantity());
                finalQuantity = line.getQuantity();
            }
        } else if (request.quantity() > 0) {
            DeckCard newLine = DeckCard.builder()
                    .card(card)
                    .deck(deck)
                    .quantity(request.quantity())
                    .build();
            deck.addCard(newLine);
            finalQuantity = newLine.getQuantity();
        }

        deck.evaluateLegality(formatProperties.getStandardValidMarks());
        deckRepository.save(deck);

        CardResponse cr = new CardResponse(
                card.getId(), card.getExternalId(), card.getName(), card.getRarity(),
                card.getCardCategory(), card.getTypes(), card.getImageUrl(),
                card.getExpansion().getExternalId(), card.getRegulationMark()
        );

        return new DeckCardResponse(cr, finalQuantity);
    }

    @Override
    @Transactional
    public void updateDeckName(UUID deckId, UUID ownerId, String newName) {

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Deck name cannot be blank");
        }

        Deck deck = deckRepository.findByIdAndOwnerId(deckId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck with ID: " + deckId + " not found"));

        deck.updateDetails(newName.trim(), deck.getIsLegal());
        deckRepository.save(deck);
    }

    private List<DeckCard> getDeckCardFromRequest(List<DeckCardRequest> requestCards) {

        return requestCards.stream().map(cardReq -> {
            Card cardEntity = cardRepository.findById(cardReq.cardId()).orElseThrow(
                    () -> new ResourceNotFoundException("Card with ID: " + cardReq.cardId() + " not found")
            );

            return DeckCard.builder()
                    .card(cardEntity)
                    .quantity(cardReq.quantity())
                    .build();
        }).toList();
    }
}
