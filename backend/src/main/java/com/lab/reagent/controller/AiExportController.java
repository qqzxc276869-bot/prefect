package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.ExportAnalysisService;
import com.lab.reagent.service.ExportAnalysisService.AnalyzeRequest;
import com.lab.reagent.service.ExportAnalysisService.AnalyzeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/export")
public class AiExportController {

    @Autowired
    private ExportAnalysisService exportAnalysisService;

    @PostMapping("/analyze")
    public Result<AnalyzeResponse> analyze(@RequestBody AnalyzeRequest req) {
        try {
            AnalyzeResponse resp = exportAnalysisService.analyze(req);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("AI导出分析失败：" + e.getMessage());
        }
    }
}


