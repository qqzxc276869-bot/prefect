package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.ApplyAssistService;
import com.lab.reagent.service.ApplyAssistService.ApplyOptimizeRequest;
import com.lab.reagent.service.ApplyAssistService.ApplyOptimizeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiApplyController {

    @Autowired
    private ApplyAssistService applyAssistService;

    /**
     * 申领表单智能优化：标准名/CAS补全、用途模板、用量建议与校验
     */
    @PostMapping("/apply-optimize")
    public Result<ApplyOptimizeResponse> optimize(@RequestBody ApplyOptimizeRequest request) {
        try {
            ApplyOptimizeResponse resp = applyAssistService.optimize(request);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("AI申领优化失败：" + e.getMessage());
        }
    }
}


