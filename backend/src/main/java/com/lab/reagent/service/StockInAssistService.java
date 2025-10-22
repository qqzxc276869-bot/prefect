package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.Reagent;
import com.lab.reagent.entity.StockInRecord;
import com.lab.reagent.mapper.StockInRecordMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockInAssistService {

    @Autowired
    private ReagentService reagentService;

    @Autowired
    private StockInRecordMapper stockInRecordMapper;

    @Autowired
    private AiService aiService;

    @Data
    public static class HintRequest {
        private String name;
        private String batchNo;
        private Double quantity;
        private String unit;
        private String model;
    }

    @Data
    public static class HintResponse {
        private String casNo;
        private String specification;
        private String dangerLevel;
        private Integer defaultExpiryMonths;
        private String recommendedLocation; // 文本建议
        private Boolean batchUnique;
        private String reasoning;
    }

    public HintResponse hint(HintRequest req) {
        // 批次唯一性校验
        boolean unique = true;
        if (req.getBatchNo() != null && !req.getBatchNo().trim().isEmpty()) {
            LambdaQueryWrapper<StockInRecord> qw = new LambdaQueryWrapper<>();
            qw.eq(StockInRecord::getBatchNo, req.getBatchNo().trim());
            unique = stockInRecordMapper.selectCount(qw) == 0;
        }

        // 从库中找名称近似的试剂，提供上下文
        List<Reagent> nearby = reagentService.list().stream()
                .filter(r -> r.getName() != null && req.getName() != null && r.getName().toLowerCase().contains(req.getName().toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());

        String instruction = "你是实验室入库智能助手。\n" +
                "根据试剂名称与历史信息，给出一个JSON：{casNo,specification,dangerLevel,defaultExpiryMonths,recommendedLocation,reasoning}\n" +
                "注意：若信息不确定可留空；recommendedLocation给出常见位置建议（文本）。严格返回JSON。";

        JSONObject payload = new JSONObject()
                .set("name", nvl(req.getName()))
                .set("quantity", req.getQuantity())
                .set("unit", nvl(req.getUnit()))
                .set("nearby", nearby.stream().map(r -> {
                    JSONObject o = new JSONObject();
                    o.set("name", r.getName());
                    o.set("casNo", r.getCasNo());
                    o.set("spec", r.getSpecification());
                    o.set("danger", r.getDangerLevel());
                    return o;
                }).collect(Collectors.toList()));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(mapOf("system", instruction));
        messages.add(mapOf("user", payload.toString()));

        String reply = aiService.chat(req.getModel(), messages);
        String json = stripFence(reply);

        HintResponse r = new HintResponse();
        try {
            cn.hutool.json.JSONObject obj = cn.hutool.json.JSONUtil.parseObj(json);
            r.setCasNo(obj.getStr("casNo"));
            r.setSpecification(obj.getStr("specification"));
            r.setDangerLevel(obj.getStr("dangerLevel"));
            r.setDefaultExpiryMonths(obj.getInt("defaultExpiryMonths"));
            r.setRecommendedLocation(obj.getStr("recommendedLocation"));
            r.setReasoning(obj.getStr("reasoning"));
        } catch (Exception e) {
            log.warn("AI入库提示解析失败，返回原文: {}", reply);
        }
        r.setBatchUnique(unique);
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


