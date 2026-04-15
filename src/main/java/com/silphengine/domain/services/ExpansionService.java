package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.dto.responses.ExpansionResponse;

import java.util.List;

public interface ExpansionService {

    public ExpansionResponse createExpansion(ExpansionRequest expansionRequest);

    public ExpansionResponse getByExternalId(String externalId);

    public void removeByExternalId(String externalId);

    public ExpansionResponse updateByExternalId(String externalId, ExpansionRequest expansionRequest);

    List<ExpansionResponse> getAllExpansions();
}
