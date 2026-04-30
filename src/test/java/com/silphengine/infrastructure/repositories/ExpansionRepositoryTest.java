package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Expansion;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ExpansionRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private ExpansionRepository expansionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldThrowException_whenExternalIdAlreadyExists() {
        // Given
        Expansion firstExpansion = Expansion.builder()
                .externalId("swsh1-1")
                .name("Sword & Shield")
                .serieName("Sword & Shield")
                .releaseDate(LocalDate.of(2020, 2, 7))
                .totalCards(216)
                .build();

        expansionRepository.saveAndFlush(firstExpansion);

        Expansion duplicateExpansion = Expansion.builder()
                .externalId("swsh1-1")
                .name("Darkness Ablaze")
                .serieName("Sword & Shield")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .totalCards(202)
                .build();

        // When & Then
        assertThatThrownBy(() -> expansionRepository.saveAndFlush(duplicateExpansion))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindExpansionByExternalId() {

        // Given
        Expansion expansion = Expansion.builder()
                .externalId("swsh1-1")
                .name("Sword & Shield")
                .serieName("Sword & Shield")
                .releaseDate(LocalDate.of(2020, 2, 7))
                .totalCards(216)
                .logoUrl("https://example.com/logo.png")
                .build();

        expansionRepository.save(expansion);

        // When
        Optional<Expansion> foundExpansion = expansionRepository.findByExternalId("swsh1-1");

        // Then
        assertThat(foundExpansion).isPresent();
        assertThat(foundExpansion.get().getName()).isEqualTo("Sword & Shield");
        assertThat(foundExpansion.get().getExternalId()).isEqualTo("swsh1-1");
    }

    @Test
    void shouldReturnEmptyWhenExternalIdDoesNotExist() {

        // When
        Optional<Expansion> foundExpansion = expansionRepository.findByExternalId("nonExistingId");

        // Then
        assertThat(foundExpansion).isEmpty();
    }

    @Test
    void shouldUpdateExpansionAutomatically_whenEntityIsModified_dueToDirtyChecking() {

        // Given:
        Expansion expansion = Expansion.builder()
                .externalId("swsh1-2")
                .name("Sword & Shield Base")
                .serieName("Sword & Shield")
                .releaseDate(LocalDate.of(2020, 2, 7))
                .totalCards(216)
                .build();

        expansionRepository.saveAndFlush(expansion);
        UUID expansionId = expansion.getId();

        entityManager.clear();

        // When
        Expansion managedExpansion = expansionRepository.findById(expansionId).orElseThrow();

        managedExpansion.updateDetails(
                "Scarlet & Violet",
                "Sword & Shield",
                LocalDate.of(2020, 2, 8),
                216,
                "https://example.com/new-logo.png"
        );

        // Should update expansion automatically
        entityManager.flush();

        entityManager.clear();

        // Then:
        Expansion updatedExpansion = expansionRepository.findById(expansionId).orElseThrow();

        assertThat(updatedExpansion.getName()).isEqualTo("Scarlet & Violet");
        assertThat(updatedExpansion.getLogoUrl()).isEqualTo("https://example.com/new-logo.png");
    }
}