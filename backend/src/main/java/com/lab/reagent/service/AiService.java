package com.lab.reagent.service;

import com.lab.reagent.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiService {

    @Autowired
    private AiProperties aiProperties;

    /**
     * 调用AI模型生成回复（使用Ollama API）
     * @param model 模型名称，如果为空则使用配置的默认模型
     * @param messages 对话消息列表
     * @return AI生成的回复内容
     */
    public String chat(String model, List<Map<String, String>> messages) {
        if (!aiProperties.isEnabled()) {
            throw new RuntimeException("AI功能未启用");
        }
        
        String useModel = (model == null || model.trim().isEmpty()) ? aiProperties.getModel() : model;
        // 兜底：如果消息为空，提供默认对话
        if (messages == null || messages.isEmpty()) {
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", "You are a helpful assistant.");
            Map<String, String> usr = new HashMap<>();
            usr.put("role", "user");
            usr.put("content", "请简要介绍你的功能。");
            messages = Arrays.asList(sys, usr);
        }
        
        try {
            return chatViaOllama(useModel, messages);
        } catch (Exception e) {
            log.error("AI服务调用异常", e);
            throw new RuntimeException("处理AI请求时发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * 通过 Ollama API 进行对话（兼容OpenAI格式）
     */
    private String chatViaOllama(String model, List<Map<String, String>> messages) {
        String baseUrl = aiProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new RuntimeException("AI服务配置错误：base-url未配置");
        }

        // 构造请求体
        JSONObject body = new JSONObject();
        body.set("model", model);
        JSONArray msgs = new JSONArray();
        for (Map<String, String> msg : messages) {
            JSONObject m = new JSONObject();
            m.set("role", msg.getOrDefault("role", "user"));
            m.set("content", msg.getOrDefault("content", ""));
            msgs.add(m);
        }
        body.set("messages", msgs);

        String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        log.info("调用Ollama API: {} with model: {}", url, model);

        HttpRequest request = HttpRequest.post(url)
                .header("Content-Type", ContentType.JSON.getValue())
                .body(body.toString())
                .timeout(60000); // Ollama本地调用可能需要更长时间，设置60秒超时

        // 如果配置了API Key，则添加Authorization头（Ollama本地部署通常不需要）
        if (aiProperties.getApiKey() != null && !aiProperties.getApiKey().trim().isEmpty()) {
            request.header("Authorization", "Bearer " + aiProperties.getApiKey());
        }

        HttpResponse resp = request.execute();

        if (resp.getStatus() < 200 || resp.getStatus() >= 300) {
            String err = resp.body();
            log.error("Ollama API调用失败，status={} body={}", resp.getStatus(), err);
            throw new RuntimeException("AI服务调用失败(" + resp.getStatus() + ")：" + err);
        }

        String respBody = resp.body();
        log.debug("Ollama API响应: {}", respBody);
        
        JSONObject json = JSONUtil.parseObj(respBody);
        if (!json.containsKey("choices")) {
            throw new RuntimeException("AI服务返回结果异常：无choices字段");
        }
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI服务返回结果为空");
        }
        JSONObject first = choices.getJSONObject(0);
        JSONObject message = first.getJSONObject("message");
        String content = message != null ? message.getStr("content") : null;
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("AI回复内容为空");
        }
        return content;
    }

    /**
     * 生成试剂库存相关的智能建议
     * @param reagentName 试剂名称
     * @param currentStock 当前库存量
     * @param unit 单位
     * @return 智能建议
     */
    public String generateInventoryAdvice(String reagentName, Integer currentStock, String unit) {
        String prompt = String.format(
            "你是一个实验室试剂管理专家。请为以下试剂提供库存管理建议：\n" +
            "试剂名称：%s\n" +
            "当前库存：%d %s\n" +
            "请分析当前库存状况，并提供专业的管理建议，包括是否需要补充库存、安全储存建议等。",
            reagentName, currentStock, unit
        );
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        
        return chat(null, Arrays.asList(userMessage));
    }

    /**
     * 生成安全处理建议
     * @param chemicalName 化学品名称
     * @param scenario 具体场景
     * @return 安全处理建议
     */
    public String generateSafetyAdvice(String chemicalName, String scenario) {
        String prompt = String.format(
            "你是一个实验室安全专家。请为以下化学品处理场景提供安全建议：\n" +
            "化学品名称：%s\n" +
            "处理场景：%s\n" +
            "请提供详细的安全操作步骤、必要的防护措施、应急处理方案等。",
            chemicalName, scenario
        );
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        
        return chat(null, Arrays.asList(userMessage));
    }
}


