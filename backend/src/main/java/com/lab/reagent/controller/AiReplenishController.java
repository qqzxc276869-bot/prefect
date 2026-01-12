package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.AiReplenishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI补货建议Controller
 */
@RestController
@RequestMapping("/api/ai/replenish")
public class AiReplenishController {
    
    @Autowired
    private AiReplenishService aiReplenishService;
    
    /**
     * 获取补货建议
     */
    @PostMapping("/suggest")
    public Result<List<Map<String, Object>>> suggest(@RequestBody Map<String, Object> params) {
        try {
            String model = (String) params.get("model");
            List<Map<String, Object>> suggestions = aiReplenishService.getReplenishSuggestions(model);
            return Result.success(suggestions);
        } catch (Exception e) {
            return Result.error("AI补货建议失败：" + e.getMessage());
        }
    }
}




