package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.RiskAnalysisService;
import com.lab.reagent.service.RiskAnalysisService.AnalyzeRequest;
import com.lab.reagent.service.RiskAnalysisService.AnalyzeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/risk")
public class AiRiskController {

    @Autowired
    private RiskAnalysisService riskAnalysisService;

    @PostMapping("/analyze")
    public Result<AnalyzeResponse> analyze(@RequestBody AnalyzeRequest req) {
        try {
            AnalyzeResponse resp = riskAnalysisService.analyze(req);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("AI风控分析失败：" + e.getMessage());
        }
    }
}


