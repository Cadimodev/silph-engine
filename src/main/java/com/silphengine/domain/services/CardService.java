package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.responses.CardResponse;

import java.util.List;

public interface CardService {

    public CardResponse createCard(CardRequest cardRequest);

    public CardResponse getByExternalId(String externalId);

    public List<CardResponse> getAllCards();

    public List<CardResponse> getByExternalExpansionId(String externalExpansionId);

    public CardResponse updateByExternalId(String externalId, CardRequest cardRequest);

    public void deleteByExternalId(String externalId);
}
