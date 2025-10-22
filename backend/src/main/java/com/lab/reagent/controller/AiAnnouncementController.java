package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.AnnouncementAssistService;
import com.lab.reagent.service.AnnouncementAssistService.GenRequest;
import com.lab.reagent.service.AnnouncementAssistService.GenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/announcement")
public class AiAnnouncementController {

    @Autowired
    private AnnouncementAssistService announcementAssistService;

    /**
     * 公告智能生成与定向推送标签建议
     */
    @PostMapping("/generate")
    public Result<GenResponse> generate(@RequestBody GenRequest request) {
        try {
            GenResponse resp = announcementAssistService.generate(request);
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("AI公告生成失败：" + e.getMessage());
        }
    }
}


