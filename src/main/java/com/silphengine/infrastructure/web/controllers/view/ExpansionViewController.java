package com.silphengine.infrastructure.web.controllers.view;

import com.silphengine.domain.dto.responses.ExpansionResponse;
import com.silphengine.domain.services.ExpansionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ExpansionViewController {

    private final ExpansionService expansionService;

    @GetMapping("/expansions")
    public String listExpansions(
            Model model,
            @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ExpansionResponse> expansionPage = expansionService.getExpansions(pageable);

        model.addAttribute("expansions", expansionPage.getContent());
        model.addAttribute("page", expansionPage);

        return "expansions";
    }
}