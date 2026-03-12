package com.lab.reagent.service;

import com.lab.reagent.entity.Application;
import com.lab.reagent.entity.Inventory;
import com.lab.reagent.mapper.ApplicationMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.*;

@Slf4j
@Service
public class ApprovePrecheckService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AiService aiService;

    @Data
    public static class PrecheckRequest {
        private Long applicationId;
        private String model;
    }

    @Data
    public static class PrecheckResponse {
        private boolean stockEnough;
        private String fifoSuggestion; // 例如：建议从批次A（最早过期）出库
        private List<String> substitutes; // 替代建议
        private List<String> cautions; // 风险提示
        private String summary;
    }

    public PrecheckResponse precheck(PrecheckRequest req) {
        Application app = applicationMapper.selectById(req.getApplicationId());
        if (app == null) throw new RuntimeException("申请不存在");

        // 统计该试剂可用库存（按有效期排序，模拟FIFO）
        LambdaQueryWrapper<Inventory> qw = new LambdaQueryWrapper<>();
        qw.eq(Inventory::getReagentId, app.getReagentId());
        List<Inventory> list = inventoryService.list(qw);
        list.sort(Comparator.comparing(Inventory::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())));

        java.math.BigDecimal total = list.stream()
                .map(inv -> inv.getQuantity() == null ? java.math.BigDecimal.ZERO : inv.getQuantity())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        boolean enough = app.getQuantity() != null && total.compareTo(app.getQuantity()) >= 0;

        String fifo = list.isEmpty() ? "无库存" : (list.get(0).getBatchNo() == null ? "建议选择最早过期批次出库" : ("建议从批次[" + list.get(0).getBatchNo() + "]出库"));

        // AI建议替代/注意事项
        String prompt = "你是实验员审批助手。请基于以下申请与库存信息，输出JSON：{substitutes[], cautions[], summary}\n" +
                "申请：名称=" + (app.getReagentName() == null ? "" : app.getReagentName()) +
                ", 数量=" + (app.getQuantity() == null ? 0 : app.getQuantity()) +
                ", 用途=" + (app.getPurpose() == null ? "" : app.getPurpose()) + "\n" +
                "库存总量=" + total + ", FIFO建议：" + fifo + "。";

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(mapOf("system", "严格返回JSON，不要多余文字。"));
        messages.add(mapOf("user", prompt));
        String reply = aiService.chatRaw(req.getModel(), messages);
        String json = stripFence(reply);

        PrecheckResponse r = new PrecheckResponse();
        r.setStockEnough(enough);
        r.setFifoSuggestion(fifo);
        try {
            cn.hutool.json.JSONObject obj = cn.hutool.json.JSONUtil.parseObj(json);
            java.util.List<String> subs = obj.getJSONArray("substitutes").toList(String.class);
            java.util.List<String> cas = obj.getJSONArray("cautions").toList(String.class);
            r.setSubstitutes(subs);
            r.setCautions(cas);
            r.setSummary(obj.getStr("summary"));
        } catch (Exception e) {
            r.setSubstitutes(Collections.emptyList());
            r.setCautions(Collections.emptyList());
            r.setSummary("AI未能解析，已提供FIFO建议");
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
}


