package com.lab.reagent.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.*;

@Slf4j
@Service
public class AnnouncementAssistService {

    @Autowired
    private AiService aiService;

    @Data
    public static class GenRequest {
        private String topic;           // 核心主题或关键词
        private String details;         // 关键信息（时间、地点、要求等）
        private String audience;        // 受众（如：本科生、有机组、经常申领酸类）
        private String tone;            // 语气（正式/通知/紧急等）
        private String model;           // 可选覆盖模型
    }

    @Data
    public static class GenResponse {
        private String title;
        private String content;
        private String summary;
        private List<String> highlights;
        private List<String> targetTags; // 定向推送标签
    }

    public GenResponse generate(GenRequest req) {
        String instruction = "你是实验室公告撰写与定向推送助手。\n" +
                "输入核心信息后，输出JSON对象：{title, content, summary, highlights[], targetTags[]}\n" +
                "要求：\n" +
                "1) title简洁明确；content条理清晰，包含要点；summary为2-3句摘要\n" +
                "2) highlights提炼3-5个重点；targetTags给出用于定向推送的标签（如‘有机化学’、‘常用酸类’等）\n" +
                "3) 严格只返回JSON，无多余文字。";

        JSONObject payload = new JSONObject()
                .set("topic", nvl(req.getTopic()))
                .set("details", nvl(req.getDetails()))
                .set("audience", nvl(req.getAudience()))
                .set("tone", nvl(req.getTone()));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(mapOf("system", instruction));
        messages.add(mapOf("user", payload.toString()));

        String reply = aiService.chat(req.getModel(), messages);
        String json = stripFence(reply);

        GenResponse r = new GenResponse();
        try {
            JSONObject obj = JSONUtil.parseObj(json);
            r.setTitle(obj.getStr("title"));
            r.setContent(obj.getStr("content"));
            r.setSummary(obj.getStr("summary"));
            JSONArray hs = obj.getJSONArray("highlights");
            r.setHighlights(hs == null ? Collections.emptyList() : hs.toList(String.class));
            JSONArray tags = obj.getJSONArray("targetTags");
            r.setTargetTags(tags == null ? Collections.emptyList() : tags.toList(String.class));
        } catch (Exception e) {
            log.warn("AI公告生成解析失败，返回原文: {}", reply);
            r.setTitle(nvl(req.getTopic()));
            r.setContent(nvl(req.getDetails()));
            r.setSummary("AI解析失败，已使用原始信息");
            r.setHighlights(Collections.emptyList());
            r.setTargetTags(Collections.emptyList());
        }
        return r;
    }

    private Map<String, String> mapOf(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private String stripFence(String text) {
        if (text == null) return "";
        String t = text.trim();
        if (t.startsWith("```")) {
            int idx = t.indexOf('\n');
            if (idx > 0) t = t.substring(idx + 1);
            int end = t.lastIndexOf("```");
            if (end > 0) t = t.substring(0, end);
        }
        return t.trim();
    }

    private String nvl(String s) { return s == null ? "" : s; }
}


