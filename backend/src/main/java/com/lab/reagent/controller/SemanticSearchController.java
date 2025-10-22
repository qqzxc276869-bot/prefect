package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.SemanticSearchService;
import com.lab.reagent.service.SemanticSearchService.SearchItem;
import com.lab.reagent.service.SemanticSearchService.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class SemanticSearchController {

    @Autowired
    private SemanticSearchService semanticSearchService;

    @PostMapping("/semantic-search")
    public Result<List<SearchItem>> semanticSearch(@RequestBody SearchRequest req) {
        try {
            List<SearchItem> items = semanticSearchService.semanticSearch(
                    req.getQuery(), req.getTopK(), req.getModel());
            return Result.success(items);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}


