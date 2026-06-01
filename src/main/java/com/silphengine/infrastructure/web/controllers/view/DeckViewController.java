package com.silphengine.infrastructure.web.controllers.view;

import com.silphengine.domain.dto.requests.DeckCardRequest;
import com.silphengine.domain.dto.responses.DeckCardResponse;
import com.silphengine.domain.dto.responses.DeckResponse;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.services.DeckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DeckViewController {

    private final DeckService deckService;

    @GetMapping("/decks")
    public String listDecks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal User user,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "name"));
        Page<DeckResponse> deckResponses = deckService.getByOwnerId(user.getId(), pageable);

        model.addAttribute("decks", deckResponses.getContent());
        model.addAttribute("page", deckResponses);
        model.addAttribute("currentPage", deckResponses.getNumber());
        model.addAttribute("totalPages", deckResponses.getTotalPages());
        model.addAttribute("baseUrl", "/decks");

        return "decks";
    }

    @PostMapping("/decks/new")
    public String createDeck(@AuthenticationPrincipal User user) {
        DeckResponse savedDeck = deckService.createEmptyDeck(user.getId());
        return "redirect:/decks/" + savedDeck.id() + "/build";
    }

    @GetMapping("/decks/{id}/build")
    public String showDeckBuilder(@PathVariable UUID id, @AuthenticationPrincipal User user, Model model) {
        DeckResponse deckResponse = deckService.getByIdAndOwnerID(id, user.getId());
        model.addAttribute("deck", deckResponse);
        return "deck-builder";
    }

    @PostMapping("/decks/{id}/cards")
    @ResponseBody
    public ResponseEntity<DeckCardResponse> handleCardChange(
            @PathVariable UUID id,
            @Valid @RequestBody DeckCardRequest request,
            @AuthenticationPrincipal User user) {

        DeckCardResponse response = deckService.updateDeckCardQuantity(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/decks/{id}/name")
    @ResponseBody
    public ResponseEntity<Void> updateDeckName(
            @PathVariable UUID id,
            @RequestParam String name,
            @AuthenticationPrincipal User user) {

        deckService.updateDeckName(id, user.getId(), name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/decks/{id}/html-list")
    public String getDeckHtmlList(@PathVariable UUID id, @AuthenticationPrincipal User user, Model model) {
        DeckResponse deckResponse = deckService.getByIdAndOwnerID(id, user.getId());
        model.addAttribute("deck", deckResponse);
        return "deck-builder :: #deckCenterList";
    }

    @PostMapping("/decks/{id}/delete")
    public String deleteDeck(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        deckService.deleteDeck(id, user.getId());
        return "redirect:/decks";
    }
}