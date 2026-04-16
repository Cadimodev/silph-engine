package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.dto.responses.ExpansionResponse;

import java.util.List;

public interface ExpansionService {

    ExpansionResponse createExpansion(ExpansionRequest expansionRequest);

    ExpansionResponse getByExternalId(String externalId);

    void removeByExternalId(String externalId);

    ExpansionResponse updateByExternalId(String externalId, ExpansionRequest expansionRequest);

    List<ExpansionResponse> getAllExpansions();
}
