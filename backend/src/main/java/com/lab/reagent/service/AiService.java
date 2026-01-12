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
     * 系统领域知识 Prompt
     */
    private static final String SYSTEM_KNOWLEDGE = 
        "### 核心业务指令 (CORE MISSION) ###\n" +
        "你不是通用AI助手。你是由【实验室化学试剂管理系统】深度集成的专用智能内核。\n" +
        "1. **身份边界**：你的服务对象仅限于化学实验室。严禁讨论电商、游戏、手机数码、北京/上海仓储等泛化场景。如果用户问“库存”，指且仅指本系统的【化学试剂库存】。\n" +
        "2. **数据主权**：随对话提供的【真实库存快照】是你唯一的知识来源。如果看到快照，请立即进入专家模式，直接列出数据或进行分析。\n" +
        "3. **禁令**：严禁回答“我无法查看数据库”或“请登录系统查看”。你看到的快照就是你从数据库“实时读取”的结果，请以“本系统当前记录...”的确定语气回复。\n" +
        "4. **安全底线**：所有建议必须符合实验室安全管理规范。";

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
        
        StringBuilder systemContent = new StringBuilder(SYSTEM_KNOWLEDGE);
        List<Map<String, String>> conversationMessages = new java.util.ArrayList<>();

        // 遍历原始消息，提取额外的系统信息并收集非系统消息
        if (messages != null) {
            for (Map<String, String> msg : messages) {
                String role = msg.get("role");
                String content = msg.get("content");
                if ("system".equals(role)) {
                    // 如果是其他系统消息（如实时快照），追加到基础指令之后
                    if (content != null && !content.contains("System Intelligence Brain")) {
                        systemContent.append("\n\n【补充系统上下文】：\n").append(content);
                    }
                } else {
                    conversationMessages.add(msg);
                }
            }
        }

        List<Map<String, String>> finalMessages = new java.util.ArrayList<>();
        
        // 1. 添加合并后的唯一系统消息
        Map<String, String> masterSys = new HashMap<>();
        masterSys.put("role", "system");
        masterSys.put("content", systemContent.toString());
        finalMessages.add(masterSys);

        // 2. 添加对话流消息
        finalMessages.addAll(conversationMessages);

        // 3. 兜底逻辑：如果没有有效对话消息，添加一个默认请求
        if (conversationMessages.isEmpty()) {
            Map<String, String> usr = new HashMap<>();
            usr.put("role", "user");
            usr.put("content", "请以实验室管理助手的身份简要介绍本系统。");
            finalMessages.add(usr);
        }
        
        try {
            return chatViaOllama(useModel, finalMessages);
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

        // 构造请求体并记录日志
        JSONObject body = new JSONObject();
        body.set("model", model);
        JSONArray msgs = new JSONArray();
        log.info("--- AI Request Messages ---");
        for (Map<String, String> msg : messages) {
            JSONObject m = new JSONObject();
            String role = msg.getOrDefault("role", "user");
            String content = msg.getOrDefault("content", "");
            m.set("role", role);
            m.set("content", content);
            msgs.add(m);
            log.info("[{}] {}", role, content.substring(0, Math.min(content.length(), 200)).replace("\n", " "));
        }
        log.info("---------------------------");
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

    /**
     * AI 生成公告内容
     * @param topic 用户输入的主题/关键词
     * @param audience 受众角色
     * @param priority 优先级级别
     * @return 包含标题和内容的AI回复
     */
    public String generateAnnouncement(String topic, String audience, String priority) {
        String audienceLabel = "ALL".equals(audience) ? "全体实验室人员" : ("TEACHER".equals(audience) ? "各位老师" : "全体同学");
        String priorityLabel = "URGENT".equals(priority) ? "紧急" : ("WARN".equals(priority) ? "重要" : "普通");

        String prompt = String.format(
            "你是一个专业的实验室行政管理员。请根据以下信息起草一份实验室公告：\n" +
            "公告主题：%s\n" +
            "目标受众：%s\n" +
            "紧急程度：%s\n\n" +
            "【输出规范(STRICT RULES)】：\n" +
            "1. **禁止**输出任何开场白、解释性文字或结束语（如“希望这能帮到你”）。\n" +
            "2. **禁止**生成带有方括号的占位符（如 [日期]、[联系方式] 等），请直接输出自然的完整句子。\n" +
            "3. **禁止**在 CONTENT 中重复包含“紧急程度”、“受众”、“标题”等结构化元数据。\n" +
            "4. **禁止**输出类似“### 核心内容 ###”或冗余的分段标识符。\n" +
            "5. CONTENT 必须仅包含公告的具体正文内容，语气严谨专业。\n" +
            "6. 必须且仅输出以下格式：\n\n" +
            "TITLE: [公告标题]\n" +
            "CONTENT: [公告正文]\n\n" +
            "【输出示例(EXAMPLE)】：\n" +
            "TITLE: 关于实验室卫生大扫除的通知\n" +
            "CONTENT: 请全体同学于本周五下午2点参加实验室大扫除，注意安全防范。感谢您的配合与支持！",
            topic, audienceLabel, priorityLabel
        );

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        return chat(null, Arrays.asList(userMessage));
    }
}


