package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Expansion;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.testcontainers.junit.jupiter.Testcontainers;


import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ExpansionRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private ExpansionRepository expansionRepository;

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
}