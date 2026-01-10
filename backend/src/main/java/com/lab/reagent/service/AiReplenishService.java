package com.lab.reagent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.Inventory;
import com.lab.reagent.entity.Reagent;
import com.lab.reagent.entity.StockOutRecord;
import com.lab.reagent.mapper.InventoryMapper;
import com.lab.reagent.mapper.ReagentMapper;
import com.lab.reagent.mapper.StockOutRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    private RestTemplate restTemplate;
    
    @Value("${ai.base-url}")
    private String aiBaseUrl;
    
    @Value("${ai.api-key}")
    private String apiKey;
    
    /**
     * 获取补货建议
     */
    public List<Map<String, Object>> getReplenishSuggestions(String model) {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        // 查询库存不足的试剂
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Inventory::getStatus, Arrays.asList("LOW", "EXPIRING", "EXPIRED"));
        List<Inventory> lowStockList = inventoryMapper.selectList(wrapper);
        
        for (Inventory inventory : lowStockList) {
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
            
            // 计算平均日消耗
            BigDecimal avgDailyConsumption = totalConsumption.divide(new BigDecimal(30), 2, BigDecimal.ROUND_HALF_UP);
            
            // 使用AI生成补货建议
            String aiSuggestion = getAiSuggestion(reagent, inventory, avgDailyConsumption, model);
            
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("reagentId", reagent.getId());
            suggestion.put("reagentName", reagent.getName());
            suggestion.put("currentStock", inventory.getQuantity());
            suggestion.put("unit", reagent.getUnit());
            suggestion.put("avgDailyConsumption", avgDailyConsumption);
            suggestion.put("warningThreshold", inventory.getWarningThreshold());
            suggestion.put("status", inventory.getStatus());
            suggestion.put("aiSuggestion", aiSuggestion);
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * 调用AI获取补货建议
     */
    private String getAiSuggestion(Reagent reagent, Inventory inventory, BigDecimal avgDailyConsumption, String model) {
        try {
            String prompt = String.format(
                "试剂名称：%s\n" +
                "当前库存：%s %s\n" +
                "预警阈值：%s %s\n" +
                "平均日消耗：%s %s\n" +
                "库存状态：%s\n" +
                "供应商到货周期：%d天\n\n" +
                "请基于以上信息，给出简洁的补货建议，包括建议采购量和理由。回答格式：建议采购量XXX，理由：...",
                reagent.getName(),
                inventory.getQuantity(), reagent.getUnit(),
                inventory.getWarningThreshold(), reagent.getUnit(),
                avgDailyConsumption, reagent.getUnit(),
                getStatusText(inventory.getStatus()),
                reagent.getSupplierLeadTime() != null ? reagent.getSupplierLeadTime() : 7
            );
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 300);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);
            
            String response = restTemplate.postForObject(aiBaseUrl + "/chat/completions", entity, String.class);
            
            JSONObject jsonResponse = JSON.parseObject(response);
            String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            
            return content.trim();
        } catch (Exception e) {
            // AI调用失败，返回基础建议
            BigDecimal suggestedQuantity = avgDailyConsumption.multiply(new BigDecimal(30));
            return String.format("建议采购量%s%s，理由：基于30天平均消耗量计算", 
                suggestedQuantity.setScale(0, BigDecimal.ROUND_UP), reagent.getUnit());
        }
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




