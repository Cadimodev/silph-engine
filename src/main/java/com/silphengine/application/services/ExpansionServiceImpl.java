package com.silphengine.application.services;

import com.silphengine.application.mappers.ExpansionMapper;
import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.entities.Expansion;
import com.silphengine.domain.services.ExpansionService;
import com.silphengine.infrastructure.repositories.ExpansionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpansionServiceImpl implements ExpansionService {

    private final ExpansionRepository expansionRepository;
    private final ExpansionMapper expansionMapper;

    @Override
    public Expansion createExpansion(ExpansionRequest expansionRequest) {

        expansionRepository.findByExternalId(expansionRequest.externalId())
                .ifPresent(e -> {
                    throw new RuntimeException("Expansion already exists with ID: " + expansionRequest.externalId());
                });

        return expansionRepository.save(expansionMapper.toEntity(expansionRequest));
    }

    @Override
    public Expansion getByExternalId(String externalId) {
        return expansionRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Expansion with ID " + externalId + " not found"));
    }

    @Override
    public void removeByExternalId(String externalId) {

        Expansion expansion =  expansionRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Expansion with ID " + externalId + " not found"));

        expansionRepository.delete(expansion);
    }

    @Override
    public Expansion updateByExternalId(ExpansionRequest expansionRequest) {

        Expansion expansion = getByExternalId(expansionRequest.externalId());
        expansionMapper.UpdateEntityFromRequest(expansion, expansionRequest);

        return expansionRepository.save(expansion);
    }
}
