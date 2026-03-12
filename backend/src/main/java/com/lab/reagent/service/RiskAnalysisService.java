package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.Application;
import com.lab.reagent.entity.StockInRecord;
import com.lab.reagent.entity.StockOutRecord;
import com.lab.reagent.mapper.ApplicationMapper;
import com.lab.reagent.mapper.StockInRecordMapper;
import com.lab.reagent.mapper.StockOutRecordMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RiskAnalysisService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private StockInRecordMapper stockInRecordMapper;

    @Autowired
    private StockOutRecordMapper stockOutRecordMapper;

    @Autowired
    private AiService aiService;

    @Data
    public static class AnalyzeRequest {
        private String from; // ISO日期时间
        private String to;   // ISO日期时间
        private String model;
    }

    @Data
    public static class AnalyzeResponse {
        private Double riskScore; // 0~100
        private List<String> anomalies; // 异常点
        private List<String> suggestions; // 管控建议
        private String summary; // 摘要
    }

    public AnalyzeResponse analyze(AnalyzeRequest req) {
        LocalDateTime from = req.getFrom() != null && !req.getFrom().isEmpty() ? LocalDateTime.parse(req.getFrom()) : LocalDateTime.now().minusDays(30);
        LocalDateTime to = req.getTo() != null && !req.getTo().isEmpty() ? LocalDateTime.parse(req.getTo()) : LocalDateTime.now();

        // 简要取数
        LambdaQueryWrapper<Application> appQ = new LambdaQueryWrapper<>();
        appQ.between(Application::getCreateTime, from, to);
        List<Application> apps = applicationMapper.selectList(appQ);

        LambdaQueryWrapper<StockOutRecord> outQ = new LambdaQueryWrapper<>();
        outQ.between(StockOutRecord::getCreateTime, from, to);
        List<StockOutRecord> outs = stockOutRecordMapper.selectList(outQ);

        LambdaQueryWrapper<StockInRecord> inQ = new LambdaQueryWrapper<>();
        inQ.between(StockInRecord::getCreateTime, from, to);
        List<StockInRecord> ins = stockInRecordMapper.selectList(inQ);

        JSONObject payload = new JSONObject();
        payload.set("range", new JSONObject().set("from", from.toString()).set("to", to.toString()));
        payload.set("applications", apps.stream().limit(500).collect(Collectors.toList()));
        payload.set("stockOuts", outs.stream().limit(500).collect(Collectors.toList()));
        payload.set("stockIns", ins.stream().limit(500).collect(Collectors.toList()));

        String instruction = "你是实验室行为风控分析助手。\n" +
                "根据申请/出入库数据识别异常行为并给出合规评分。严格返回JSON：\n" +
                "{riskScore(0-100), anomalies[], suggestions[], summary}";

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(mapOf("system", instruction));
        messages.add(mapOf("user", payload.toString()));

        String reply = aiService.chatRaw(req.getModel(), messages);
        String json = stripFence(reply);

        AnalyzeResponse resp = new AnalyzeResponse();
        try {
            cn.hutool.json.JSONObject obj = cn.hutool.json.JSONUtil.parseObj(json);
            resp.setRiskScore(obj.getDouble("riskScore"));
            JSONArray an = obj.getJSONArray("anomalies");
            resp.setAnomalies(an == null ? Collections.emptyList() : an.toList(String.class));
            JSONArray su = obj.getJSONArray("suggestions");
            resp.setSuggestions(su == null ? Collections.emptyList() : su.toList(String.class));
            resp.setSummary(obj.getStr("summary"));
        } catch (Exception e) {
            log.warn("AI风控结果解析失败，返回原文: {}", reply);
            resp.setRiskScore(60.0);
            resp.setAnomalies(Collections.singletonList("AI未能解析，建议人工复核"));
            resp.setSuggestions(Collections.singletonList("缩小时间范围或补充更多上下文"));
            resp.setSummary("自动兜底结果");
        }
        return resp;
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
}


