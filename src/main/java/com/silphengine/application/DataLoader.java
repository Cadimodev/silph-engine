package com.silphengine.application;

import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.services.ExpansionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ExpansionService expansionService;

    @Override
    public void run(String... args) throws Exception  {

        System.out.println("**** Silph Engine: Loading test data ****");

        try {
            ExpansionRequest expansionRequest = new ExpansionRequest(
                    "sv1",
                    "Scarlet & Violet",
                    "Scarlet & Violet",
                    LocalDate.of(2023, 3, 31),
                    198,
                    "https://images.pokemontcg.io/sv1/logo.png"
            );

            expansionService.createExpansion(expansionRequest);
            System.out.println("Expansion successfully created! -> " + expansionRequest.name());

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("**** Silph Engine: End test data load ****");
    }
}
