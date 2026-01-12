package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.Inventory;
import com.lab.reagent.entity.Reagent;
import com.lab.reagent.entity.StockInRecord;
import com.lab.reagent.entity.StorageLocation;
import com.lab.reagent.mapper.InventoryMapper;
import com.lab.reagent.mapper.StockInRecordMapper;
import com.lab.reagent.mapper.StorageLocationMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private InventoryMapper inventoryMapper;

    @Autowired
    private StorageLocationMapper storageLocationMapper;

    @Autowired
    private AiService aiService;

    @Data
    public static class HintRequest {
        private Long reagentId; // 新增：试剂ID，用于查询历史记录
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
        private String recommendedLocation; // 文本建议（保留用于AI推测）
        private Boolean batchUnique;
        private String reasoning;
        // 新增：基于历史记录的智能建议字段
        private String suggestedExpiryDate; // 建议有效期
        private Long suggestedLocationId; // 建议存放位置ID
        private String suggestedLocationName; // 建议存放位置名称
        private BigDecimal suggestedUnitPrice; // 建议单价
        private String suggestedSupplier; // 建议供应商
        private String suggestedRemark; // 建议备注
    }

    public HintResponse hint(HintRequest req) {
        // 批次唯一性校验
        boolean unique = true;
        if (req.getBatchNo() != null && !req.getBatchNo().trim().isEmpty()) {
            LambdaQueryWrapper<StockInRecord> qw = new LambdaQueryWrapper<>();
            qw.eq(StockInRecord::getBatchNo, req.getBatchNo().trim());
            unique = stockInRecordMapper.selectCount(qw) == 0;
        }

        HintResponse r = new HintResponse();
        r.setBatchUnique(unique);

        // 如果提供了reagentId，优先从历史记录中提取建议
        if (req.getReagentId() != null) {
            fillHistoricalSuggestions(req.getReagentId(), r);
        }

        // 仍然调用AI获取基础信息（CAS号、规格等）作为补充
        List<Reagent> nearby = reagentService.list().stream()
                .filter(rg -> rg.getName() != null && req.getName() != null && rg.getName().toLowerCase().contains(req.getName().toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());

        String instruction = "你是实验室入库智能助手。\n" +
                "根据试剂名称与历史信息，给出一个JSON：{casNo,specification,dangerLevel,defaultExpiryMonths,recommendedLocation,reasoning}\n" +
                "注意：若信息不确定可留空；recommendedLocation给出常见位置建议（文本）。严格返回JSON。";

        JSONObject payload = new JSONObject()
                .set("name", nvl(req.getName()))
                .set("quantity", req.getQuantity())
                .set("unit", nvl(req.getUnit()))
                .set("nearby", nearby.stream().map(rg -> {
                    JSONObject o = new JSONObject();
                    o.set("name", rg.getName());
                    o.set("casNo", rg.getCasNo());
                    o.set("spec", rg.getSpecification());
                    o.set("danger", rg.getDangerLevel());
                    return o;
                }).collect(Collectors.toList()));

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(mapOf("system", instruction));
        messages.add(mapOf("user", payload.toString()));

        String reply = aiService.chat(req.getModel(), messages);
        String json = stripFence(reply);

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

        return r;
    }

    /**
     * 基于历史入库记录填充智能建议
     */
    private void fillHistoricalSuggestions(Long reagentId, HintResponse response) {
        try {
            // 查询该试剂最近5条入库记录
            LambdaQueryWrapper<StockInRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StockInRecord::getReagentId, reagentId);
            wrapper.orderByDesc(StockInRecord::getCreateTime);
            wrapper.last("LIMIT 5");
            List<StockInRecord> recentRecords = stockInRecordMapper.selectList(wrapper);

            if (recentRecords.isEmpty()) {
                return; // 无历史记录，跳过
            }

            // 1. 提取最常用的存放位置（众数）
            Map<Long, Long> locationFrequency = recentRecords.stream()
                    .filter(rec -> rec.getLocationId() != null)
                    .collect(Collectors.groupingBy(StockInRecord::getLocationId, Collectors.counting()));
            
            if (!locationFrequency.isEmpty()) {
                Long mostCommonLocationId = Collections.max(locationFrequency.entrySet(), Map.Entry.comparingByValue()).getKey();
                response.setSuggestedLocationId(mostCommonLocationId);
                
                StorageLocation location = storageLocationMapper.selectById(mostCommonLocationId);
                if (location != null) {
                    response.setSuggestedLocationName(location.getFullLocation());
                }
            }

            // 2. 提取最常用的供应商
            Map<String, Long> supplierFrequency = recentRecords.stream()
                    .filter(rec -> rec.getSupplier() != null && !rec.getSupplier().trim().isEmpty())
                    .collect(Collectors.groupingBy(StockInRecord::getSupplier, Collectors.counting()));
            
            if (!supplierFrequency.isEmpty()) {
                String mostCommonSupplier = Collections.max(supplierFrequency.entrySet(), Map.Entry.comparingByValue()).getKey();
                response.setSuggestedSupplier(mostCommonSupplier);
            }

            // 3. 计算平均单价或取最近的单价
            StockInRecord latestRecord = recentRecords.get(0);
            if (latestRecord.getPurchasePrice() != null && latestRecord.getQuantity() != null && latestRecord.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal unitPrice = latestRecord.getPurchasePrice().divide(latestRecord.getQuantity(), 2, BigDecimal.ROUND_HALF_UP);
                response.setSuggestedUnitPrice(unitPrice);
            }

            // 4. 提取最常用的备注（如果有）
            Map<String, Long> remarkFrequency = recentRecords.stream()
                    .filter(rec -> rec.getRemark() != null && !rec.getRemark().trim().isEmpty())
                    .collect(Collectors.groupingBy(StockInRecord::getRemark, Collectors.counting()));
            
            if (!remarkFrequency.isEmpty()) {
                String mostCommonRemark = Collections.max(remarkFrequency.entrySet(), Map.Entry.comparingByValue()).getKey();
                response.setSuggestedRemark(mostCommonRemark);
            }

            // 5. 计算建议有效期（基于当前日期 + 试剂保质期月数）
            Reagent reagent = reagentService.getById(reagentId);
            if (reagent != null && response.getDefaultExpiryMonths() == null) {
                response.setDefaultExpiryMonths(12); // 默认12个月
            }
            
            int expiryMonths = response.getDefaultExpiryMonths() != null ? response.getDefaultExpiryMonths() : 12;
            LocalDate suggestedExpiry = LocalDate.now().plusMonths(expiryMonths);
            response.setSuggestedExpiryDate(suggestedExpiry.toString());

        } catch (Exception e) {
            log.warn("提取历史入库建议失败: {}", e.getMessage());
            // 失败不影响整体功能，继续返回AI推测结果
        }
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


