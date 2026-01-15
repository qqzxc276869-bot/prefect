package com.lab.reagent.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.Inventory;
import com.lab.reagent.entity.Reagent;
import com.lab.reagent.entity.StockOutRecord;
import com.lab.reagent.mapper.InventoryMapper;
import com.lab.reagent.mapper.ReagentMapper;
import com.lab.reagent.mapper.StockOutRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI补货建议Service
 */
@Service
public class AiReplenishService {
    
    @Autowired
    private InventoryMapper inventoryMapper;
    
    @Autowired
    private ReagentMapper reagentMapper;
    
    @Autowired
    private StockOutRecordMapper stockOutRecordMapper;

    @Autowired
    private AiService aiService;
    
    /**
     * 获取补货建议
     */
    public List<Map<String, Object>> getReplenishSuggestions(String model) {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        // 查询库存不足的试剂
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Inventory::getStatus, Arrays.asList("LOW", "EXPIRING", "EXPIRED"));
        List<Inventory> lowStockList = inventoryMapper.selectList(wrapper);
        
        // 性能优化：限制处理数量，防止Prompt过长
        List<Inventory> targetList = lowStockList.stream().limit(15).collect(Collectors.toList());
        if (targetList.isEmpty()) {
            return suggestions;
        }

        // 1. 准备批量数据
        JSONArray itemsPayload = new JSONArray();
        Map<Long, BigDecimal> consumptionMap = new HashMap<>();

        for (Inventory inventory : targetList) {
            Reagent reagent = reagentMapper.selectById(inventory.getReagentId());
            if (reagent == null) continue;

            // 计算最近30天的消耗量
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            LambdaQueryWrapper<StockOutRecord> outWrapper = new LambdaQueryWrapper<>();
            outWrapper.eq(StockOutRecord::getReagentId, reagent.getId());
            outWrapper.ge(StockOutRecord::getCreateTime, thirtyDaysAgo);
            List<StockOutRecord> outRecords = stockOutRecordMapper.selectList(outWrapper);

            BigDecimal totalConsumption = outRecords.stream()
                    .map(StockOutRecord::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgDailyConsumption = totalConsumption.divide(new BigDecimal(30), 2, BigDecimal.ROUND_HALF_UP);
            consumptionMap.put(reagent.getId(), avgDailyConsumption);

            JSONObject item = new JSONObject();
            item.set("id", reagent.getId());
            item.set("name", reagent.getName());
            item.set("current", inventory.getQuantity() + " " + reagent.getUnit());
            item.set("warn", inventory.getWarningThreshold());
            item.set("dailyUse", avgDailyConsumption);
            item.set("status", getStatusText(inventory.getStatus()));
            item.set("leadTime", reagent.getSupplierLeadTime() != null ? reagent.getSupplierLeadTime() : 7);
            itemsPayload.add(item);
        }

        // 2. 构建批量Prompt
        String instruction = "你是实验室库存管理专家。请分析以下试剂的库存与消耗情况，给出补货建议。\n" +
                "请返回一个JSON数组，每个元素包含：{id, suggestion, quantity}。\n" +
                "rules:\n" +
                "- quantity: 建议采购数量(数字)\n" +
                "- suggestion: 简短的补货理由（如'库存告急，消耗快'）\n" +
                "- 基于消耗率和到货周期估算安全库存。\n" +
                "仅返回JSON数组，不要Markdown格式。";

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sys = new HashMap<>();
        sys.put("role", "system");
        sys.put("content", instruction);
        messages.add(sys);

        Map<String, String> usr = new HashMap<>();
        usr.put("role", "user");
        usr.put("content", itemsPayload.toString());
        messages.add(usr);

        // 3. 调用 AI (一次调用)
        String reply = aiService.chat(model, messages);
        String jsonText = stripFence(reply);
        
        // 4. 解析结果并组装
        Map<Long, JSONObject> aiResults = new HashMap<>();
        try {
            if (JSONUtil.isTypeJSONArray(jsonText)) {
                JSONArray arr = JSONUtil.parseArray(jsonText);
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    aiResults.put(o.getLong("id"), o);
                }
            }
        } catch (Exception e) {
            // 解析失败，忽略AI建议，使用默认计算
        }

        // 5. 生成最终列表
        for (Inventory inventory : targetList) {
            Reagent reagent = reagentMapper.selectById(inventory.getReagentId());
            if (reagent == null) continue;
            
            BigDecimal avgDaily = consumptionMap.getOrDefault(reagent.getId(), BigDecimal.ZERO);
            
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("reagentId", reagent.getId());
            suggestion.put("reagentName", reagent.getName());
            suggestion.put("currentStock", inventory.getQuantity());
            suggestion.put("unit", reagent.getUnit());
            suggestion.put("avgDailyConsumption", avgDaily);
            suggestion.put("warningThreshold", inventory.getWarningThreshold());
            suggestion.put("status", inventory.getStatus());

            JSONObject aiRes = aiResults.get(reagent.getId());
            if (aiRes != null) {
                String reason = aiRes.getStr("suggestion");
                BigDecimal q = aiRes.getBigDecimal("quantity");
                suggestion.put("aiSuggestion", "建议采购 " + q + " " + reagent.getUnit() + "，" + reason);
            } else {
                // 回退逻辑
                BigDecimal suggestedQuantity = avgDaily.multiply(new BigDecimal(30));
                suggestion.put("aiSuggestion", String.format("建议采购%s%s，理由：基于30天平均消耗量计算", 
                    suggestedQuantity.setScale(0, BigDecimal.ROUND_UP), reagent.getUnit()));
            }
            suggestions.add(suggestion);
        }
        
        return suggestions;
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
    
    private String getStatusText(String status) {
        if (status == null) return "正常";
        switch (status) {
            case "NORMAL": return "正常";
            case "LOW": return "库存不足";
            case "EXPIRING": return "即将过期";
            case "EXPIRED": return "已过期";
            default: return status;
        }
    }
}




