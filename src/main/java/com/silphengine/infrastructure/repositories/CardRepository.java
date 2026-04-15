package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    public Optional<Card> findByExternalId(String externalId);

    public List<Card> findByExpansion_ExternalId(String expansionExternalId);
}
