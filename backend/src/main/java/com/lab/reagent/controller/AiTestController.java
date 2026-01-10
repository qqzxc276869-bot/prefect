package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.config.AiProperties;
import com.lab.reagent.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai-test")
public class AiTestController {

    @Autowired
    private AiService aiService;

    @Autowired
    private AiProperties aiProperties;

    /**
     * AI功能测试接口
     */
    @GetMapping("/test")
    public Result<String> testAI() {
        try {
            log.info("开始测试AI功能...");
            
            // 使用配置的默认模型进行测试
            String result = aiService.chat(null, null);
            log.info("AI测试成功，返回结果：" + result);
            
            return Result.success("AI功能正常，测试完成");
        } catch (Exception e) {
            log.error("AI功能测试失败", e);
            return Result.error("AI功能测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查AI配置状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getAIStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("enabled", aiProperties.isEnabled());
            status.put("model", aiProperties.getModel());
            status.put("baseUrl", aiProperties.getBaseUrl());
            status.put("apiKeyConfigured", aiProperties.getApiKey() != null && !aiProperties.getApiKey().trim().isEmpty());
            status.put("status", "ready");
            
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取AI状态失败", e);
            return Result.error("获取AI状态失败：" + e.getMessage());
        }
    }
}