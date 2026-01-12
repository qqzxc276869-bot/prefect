package com.lab.reagent.service;

import com.lab.reagent.entity.Reagent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SemanticSearchService {

    @Autowired
    private ReagentService reagentService;

    @Autowired
    private AiService aiService;

    @Data
    public static class SearchRequest {
        private String query;
        private Integer topK;
        private String model;
    }

    @Data
    public static class SearchItem {
        private Long id;
        private String name;
        private String casNo;
        private String specification;
        private Double score;
        private String reason;
    }

    public List<SearchItem> semanticSearch(String query, Integer topK, String model) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        int k = (topK == null || topK <= 0 || topK > 20) ? 8 : topK;

        // 性能优化：减少候选数量，提升响应速度
        List<Reagent> all = reagentService.list();
        List<Reagent> candidates = all.stream()
                .limit(50)  // 从300减少到50，大幅提升速度
                .collect(Collectors.toList());

        // 组装候选JSON，尽量精简
        JSONArray candidateArr = new JSONArray();
        for (Reagent r : candidates) {
            JSONObject obj = new JSONObject();
            obj.set("id", r.getId());
            obj.set("name", r.getName());
            obj.set("casNo", r.getCasNo());
            // 去除specification字段，进一步减少数据量
            candidateArr.add(obj);
        }

        String instruction = "你是实验室试剂检索助手。\n" +
                "给定用户的自然语言查询与候选试剂列表，请返回与查询最相关的TopK试剂。\n" +
                "要求：\n" +
                "1) 兼容错别字/简称/用途描述（如‘酯化反应试剂’）\n" +
                "2) 按相关度与历史常见性（若能从描述推断）排序\n" +
                "3) 严格输出JSON数组，每个元素字段：id,name,casNo,specification,score,reason\n" +
                "4) score取值0~1，小数，越高越相关；reason给出简短匹配理由\n" +
                "不要输出多余文字。";

        // 构造messages
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sys = new HashMap<>();
        sys.put("role", "system");
        sys.put("content", instruction);
        messages.add(sys);

        JSONObject userPayload = new JSONObject();
        userPayload.set("query", query);
        userPayload.set("topK", k);
        userPayload.set("candidates", candidateArr);

        Map<String, String> usr = new HashMap<>();
        usr.put("role", "user");
        usr.put("content", userPayload.toString());
        messages.add(usr);

        String reply = aiService.chat(model != null && !model.isEmpty() ? model : null, messages);

        // 解析AI返回
        String jsonText = stripFence(reply);
        List<SearchItem> result = new ArrayList<>();
        try {
            if (JSONUtil.isTypeJSONArray(jsonText)) {
                JSONArray arr = JSONUtil.parseArray(jsonText);
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    SearchItem item = new SearchItem();
                    item.setId(o.getLong("id"));
                    item.setName(o.getStr("name"));
                    item.setCasNo(o.getStr("casNo"));
                    item.setSpecification(o.getStr("specification"));
                    item.setScore(o.getDouble("score"));
                    item.setReason(o.getStr("reason"));
                    result.add(item);
                }
            }
        } catch (Exception e) {
            log.warn("AI语义检索结果解析失败，返回原文: {}", reply);
        }

        // 兜底：若AI未返回有效JSON，则进行基础模糊匹配
        if (result.isEmpty()) {
            String q = query.toLowerCase();
            for (Reagent r : all) {
                String name = r.getName() == null ? "" : r.getName();
                String cas = r.getCasNo() == null ? "" : r.getCasNo();
                if (name.toLowerCase().contains(q) || cas.toLowerCase().contains(q)) {
                    SearchItem item = new SearchItem();
                    item.setId(r.getId());
                    item.setName(r.getName());
                    item.setCasNo(r.getCasNo());
                    item.setSpecification(r.getSpecification());
                    item.setScore(0.6);
                    item.setReason("基础模糊匹配");
                    result.add(item);
                    if (result.size() >= k) break;
                }
            }
        }

        // 只保留TopK
        return result.stream()
                .sorted((a, b) -> Double.compare(b.getScore() == null ? 0 : b.getScore(), a.getScore() == null ? 0 : a.getScore()))
                .limit(k)
                .collect(Collectors.toList());
    }

    private String stripFence(String text) {
        if (text == null) return "";
        String t = text.trim();
        if (t.startsWith("```")) {
            int idx = t.indexOf('\n');
            if (idx > 0) {
                t = t.substring(idx + 1);
            }
            int end = t.lastIndexOf("```");
            if (end > 0) {
                t = t.substring(0, end);
            }
        }
        return t.trim();
    }
}


