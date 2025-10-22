package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.ApprovePrecheckService;
import com.lab.reagent.service.ApprovePrecheckService.PrecheckRequest;
import com.lab.reagent.service.ApprovePrecheckService.PrecheckResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/approve")
public class AiApproveController {

    @Autowired
    private ApprovePrecheckService approvePrecheckService;

    @PostMapping("/precheck")
    public Result<PrecheckResponse> precheck(@RequestBody PrecheckRequest req) {
        try {
            PrecheckResponse resp = approvePrecheckService.precheck(req);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("AI预审失败：" + e.getMessage());
        }
    }
}


