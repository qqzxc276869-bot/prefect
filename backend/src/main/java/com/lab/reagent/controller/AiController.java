package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.AiService;
import com.lab.reagent.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 通用AI对话接口
     * 请求体示例：
     * {
     *   "model": "qwen2.5:0.5b",  // 可选，不传则使用配置的本地默认模型
     *   "messages": [
     *     {"role":"system","content":"You are a helpful assistant."},
     *     {"role":"user","content":"帮我生成一条公告"}
     *   ]
     * }
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestHeader(value = "Authorization", required = false) String authorization,
                               @RequestBody Map<String, Object> body) {
        try {
            // 验证用户是否已登录（移除管理员权限限制）
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return Result.error("未授权：缺少令牌");
            }
            String token = authorization.substring(7);
            String role = jwtUtil.getRoleFromToken(token);
            if (role == null) {
                return Result.error("未授权：令牌无效");
            }
            
            String model = body.get("model") == null ? null : body.get("model").toString();
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");
            String content = aiService.chat(model, messages);
            return Result.success(content);
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return Result.error("AI对话失败：" + e.getMessage());
        }
    }

    /**
     * 生成库存管理建议
     */
    @PostMapping("/inventory-advice")
    public Result<String> generateInventoryAdvice(
            @RequestParam String reagentName,
            @RequestParam Integer currentStock,
            @RequestParam String unit) {
        try {
            String advice = aiService.generateInventoryAdvice(reagentName, currentStock, unit);
            return Result.success(advice);
        } catch (Exception e) {
            log.error("生成库存建议失败", e);
            return Result.error("生成库存建议失败：" + e.getMessage());
        }
    }

    /**
     * 生成安全处理建议
     */
    @PostMapping("/safety-advice")
    public Result<String> generateSafetyAdvice(
            @RequestParam String chemicalName,
            @RequestParam String scenario) {
        try {
            String advice = aiService.generateSafetyAdvice(chemicalName, scenario);
            return Result.success(advice);
        } catch (Exception e) {
            log.error("生成安全建议失败", e);
            return Result.error("生成安全建议失败：" + e.getMessage());
        }
    }

    /**
     * AI 生成公告建议
     */
    @PostMapping("/announcement-generate")
    public Result<String> generateAnnouncement(
            @RequestParam String topic,
            @RequestParam(defaultValue = "ALL") String audience,
            @RequestParam(defaultValue = "INFO") String priority) {
        try {
            String content = aiService.generateAnnouncement(topic, audience, priority);
            return Result.success(content);
        } catch (Exception e) {
            log.error("AI 生成公告失败", e);
            return Result.error("AI 生成公告失败：" + e.getMessage());
        }
    }
}


