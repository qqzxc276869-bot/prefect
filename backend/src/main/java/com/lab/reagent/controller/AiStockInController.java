package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.StockInAssistService;
import com.lab.reagent.service.StockInAssistService.HintRequest;
import com.lab.reagent.service.StockInAssistService.HintResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/stockin")
public class AiStockInController {

    @Autowired
    private StockInAssistService stockInAssistService;

    /**
     * 入库智能提示
     */
    @PostMapping("/hint")
    public Result<HintResponse> hint(@RequestBody HintRequest req) {
        try {
            HintResponse resp = stockInAssistService.hint(req);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("AI入库提示失败：" + e.getMessage());
        }
    }
}


