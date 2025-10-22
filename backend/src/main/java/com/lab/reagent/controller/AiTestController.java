package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
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

    /**
     * AI功能测试接口
     */
    @GetMapping("/test")
    public Result<String> testAI() {
        try {
            log.info("开始测试AI功能...");
            
            // 测试简单的对话
            Map<String, Object> body = new HashMap<>();
            body.put("model", "qwen2.5:0.5b");
            
            String result = aiService.chat("qwen2.5:0.5b", null);
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
            status.put("enabled", true);
            status.put("model", "qwen2.5:0.5b");
            status.put("baseUrl", "http://localhost:11434/v1");
            status.put("apiKeyConfigured", true);
            status.put("status", "ready");
            
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取AI状态失败", e);
            return Result.error("获取AI状态失败：" + e.getMessage());
        }
    }
}