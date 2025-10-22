package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.StockInRecord;
import com.lab.reagent.entity.StockOutRecord;
import com.lab.reagent.mapper.StockInRecordMapper;
import com.lab.reagent.mapper.StockOutRecordMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ExportAnalysisService {

    @Autowired
    private StockInRecordMapper stockInRecordMapper;

    @Autowired
    private StockOutRecordMapper stockOutRecordMapper;

    @Autowired
    private AiService aiService;

    @Data
    public static class AnalyzeRequest {
        private String from; // yyyy-MM-dd
        private String to;   // yyyy-MM-dd
        private String model;
    }

    @Data
    public static class AnalyzeResponse {
        private String summary; // 概要
        private List<String> insights; // 关键洞察
        private List<String> actions;  // 采购与管理建议
    }

    public AnalyzeResponse analyze(AnalyzeRequest req) {
        LocalDate fromD = req.getFrom() != null && !req.getFrom().isEmpty() ? LocalDate.parse(req.getFrom()) : LocalDate.now().minusDays(30);
        LocalDate toD = req.getTo() != null && !req.getTo().isEmpty() ? LocalDate.parse(req.getTo()) : LocalDate.now();

        LocalDateTime from = fromD.atStartOfDay();
        LocalDateTime to = toD.atTime(23, 59, 59);

        LambdaQueryWrapper<StockOutRecord> outQ = new LambdaQueryWrapper<>();
        outQ.between(StockOutRecord::getCreateTime, from, to);
        int outCount = stockOutRecordMapper.selectCount(outQ).intValue();

        LambdaQueryWrapper<StockInRecord> inQ = new LambdaQueryWrapper<>();
        inQ.between(StockInRecord::getCreateTime, from, to);
        int inCount = stockInRecordMapper.selectCount(inQ).intValue();

        JSONObject payload = new JSONObject()
                .set("range", new JSONObject().set("from", fromD.toString()).set("to", toD.toString()))
                .set("summaryData", new JSONObject().set("stockOutCount", outCount).set("stockInCount", inCount))
                .set("goal", "用于导出报表的AI解读：生成消耗TopN、补货建议、预警趋势结论");

        String instruction = "你是实验室库存报表分析助手。\n" +
                "根据时间段内的出入库汇总信息，输出JSON：{summary, insights[], actions[]}。精炼、中文。";

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(mapOf("system", instruction));
        messages.add(mapOf("user", payload.toString()));

        String reply = aiService.chat(req.getModel(), messages);
        String json = stripFence(reply);

        AnalyzeResponse resp = new AnalyzeResponse();
        try {
            cn.hutool.json.JSONObject obj = cn.hutool.json.JSONUtil.parseObj(json);
            resp.setSummary(obj.getStr("summary"));
            resp.setInsights(obj.getJSONArray("insights").toList(String.class));
            resp.setActions(obj.getJSONArray("actions").toList(String.class));
        } catch (Exception e) {
            log.warn("AI导出分析解析失败，返回原文: {}", reply);
            resp.setSummary("AI未能解析，建议补充更详细的明细数据");
            resp.setInsights(Collections.emptyList());
            resp.setActions(Collections.emptyList());
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


