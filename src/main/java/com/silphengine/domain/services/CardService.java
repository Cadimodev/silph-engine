package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.responses.CardResponse;

import java.util.List;

public interface CardService {

    CardResponse createCard(CardRequest cardRequest);

    CardResponse getByExternalId(String externalId);

    List<CardResponse> getAllCards();

    List<CardResponse> getByExternalExpansionId(String externalExpansionId);

    CardResponse updateByExternalId(String externalId, CardRequest cardRequest);

    void deleteByExternalId(String externalId);
}
