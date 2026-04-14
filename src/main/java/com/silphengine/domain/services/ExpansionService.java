package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.entities.Expansion;

public interface ExpansionService {

    public Expansion createExpansion(ExpansionRequest expansionRequest);

    public Expansion getByExternalId(String externalId);

    public void removeByExternalId(String externalId);

    public Expansion updateByExternalId(ExpansionRequest expansionRequest);
}
